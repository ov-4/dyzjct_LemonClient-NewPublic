package dev.lemonclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.lemonclient.LemonClient;
import dev.lemonclient.commands.Command;
import dev.lemonclient.commands.arguments.PlayerListEntryArgumentType;
import dev.lemonclient.utils.Utils;
import dev.lemonclient.utils.network.Executor;
import dev.lemonclient.utils.network.Http;
import dev.lemonclient.utils.player.ChatUtils;
import dev.lemonclient.utils.player.PlayerUtils;
import dev.lemonclient.utils.render.color.Color;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.CommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class NameHistoryCommand extends Command {
    public NameHistoryCommand() {
        super("name-history", "Provides a list of a players previous names from the laby.net api.", "history", "names");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("player", PlayerListEntryArgumentType.create()).executes(context -> {
            Executor.execute(() -> {
                PlayerListEntry lookUpTarget = PlayerListEntryArgumentType.get(context);
                UUID uuid = lookUpTarget.getProfile().getId();

                NameHistory history = Http.get("https://laby.net/api/v2/user/" + uuid + "/get-profile").sendJson(NameHistory.class);

                if (history == null || history.username_history == null || history.username_history.length == 0) {
                    error("There was an error fetching that users name history.");
                    return;
                }

                String name = lookUpTarget.getProfile().getName();
                MutableText initial = Text.literal(name);
                initial.append(Text.literal(name.endsWith("s") ? "'" : "'s"));

                Color nameColor = PlayerUtils.getPlayerColor(LemonClient.mc.world.getPlayerByUuid(uuid), Utils.WHITE);

                initial.setStyle(initial.getStyle()
                    .withColor(TextColor.fromRgb(nameColor.getPacked()))
                    .withClickEvent(new ClickEvent(
                            ClickEvent.Action.OPEN_URL,
                            "https://laby.net/@" + name
                        )
                    )
                    .withHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        Text.literal("View on laby.net")
                            .formatted(Formatting.YELLOW)
                            .formatted(Formatting.ITALIC)
                    ))
                );

                info(initial.append(Text.literal(" Username History:").formatted(Formatting.GRAY)));

                for (Name entry : history.username_history) {
                    MutableText nameText = Text.literal(entry.name);
                    nameText.formatted(Formatting.AQUA);

                    if (entry.changed_at != null && entry.changed_at.getTime() != 0) {
                        MutableText changed = Text.literal("Changed at: ");
                        changed.formatted(Formatting.GRAY);

                        DateFormat formatter = new SimpleDateFormat("hh:mm:ss, dd/MM/yyyy");
                        changed.append(Text.literal(formatter.format(entry.changed_at)).formatted(Formatting.WHITE));

                        nameText.setStyle(nameText.getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, changed)));
                    }

                    if (!entry.accurate) {
                        MutableText text = Text.literal("*").formatted(Formatting.WHITE);

                        text.setStyle(text.getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("This name history entry is not accurate according to laby.net"))));

                        nameText.append(text);
                    }

                    ChatUtils.sendMsg(nameText);
                }
            });

            return SINGLE_SUCCESS;
        }));
    }

    private static class NameHistory {
        public Name[] username_history;
    }

    private static class Name {
        public String name;
        public Date changed_at;
        public boolean accurate;
    }
}
