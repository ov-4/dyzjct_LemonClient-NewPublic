package dev.lemonclient.systems.accounts.types;

import com.mojang.authlib.Environment;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import dev.lemonclient.mixin.IMinecraftClient;
import dev.lemonclient.mixin.IYggdrasilMinecraftSessionService;
import dev.lemonclient.systems.accounts.Account;
import dev.lemonclient.systems.accounts.AccountType;
import dev.lemonclient.utils.network.Http;
import net.minecraft.client.util.Session;

import java.util.Optional;

import static dev.lemonclient.LemonClient.mc;

public class EasyMCAccount extends Account<EasyMCAccount> {

    private static final Environment ENVIRONMENT = Environment.create("https://authserver.mojang.com", "https://api.mojang.com", "https://sessionserver.easymc.io", "https://api.minecraftservices.com", "EasyMC");
    private static final YggdrasilAuthenticationService SERVICE = new YggdrasilAuthenticationService(((IMinecraftClient) mc).getProxy(), "", ENVIRONMENT);

    public EasyMCAccount(String token) {
        super(AccountType.EasyMC, token);
    }

    @Override
    public boolean fetchInfo() {
        // we set the name to the session id after we redeem the token - the token length is 20, session id length is 43
        if (name.length() == 43) return true;

        AuthResponse res = Http.post("https://api.easymc.io/v1/token/redeem")
            .bodyJson("{\"token\":\"" + name + "\"}")
            .sendJson(AuthResponse.class);

        if (res != null) {
            cache.username = res.mcName;
            cache.uuid = res.uuid;

            name = res.session;

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean login() {
        applyLoginEnvironment(SERVICE, IYggdrasilMinecraftSessionService.createYggdrasilMinecraftSessionService(SERVICE, ENVIRONMENT));
        setSession(new Session(cache.username, cache.uuid, name, Optional.empty(), Optional.empty(), Session.AccountType.MOJANG));

        cache.loadHead();
        return true;
    }

    private static class AuthResponse {
        public String mcName;
        public String uuid;
        public String session;
        public String message;
    }
}
