package com.github.xhiroyui.orinbot.modules;

import com.github.xhiroyui.orinbot.util.CommandUtil;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public class CommandHandler {
    private final DiscordClient client;
    private Map<Long, String> guildPrefixes;

    public CommandHandler(DiscordClient client) {
        this.client = client;
        CommandUtil.initializeGuildPrefixes();
    }

    public Flux<Void> handleMCEvent() {
        return client.getEventDispatcher()
                .on(MessageCreateEvent.class)
                .filter(mce -> mce.getMessage().getContent().isPresent())
                .filter(mce -> mce.getMessage().getAuthor().map(author -> !author.isBot()).orElse(false))
                .filter(this::checkPrefix)
                .flatMap(mce -> processCommand(trimCommand(mce), mce))
                .share();
    }

    private boolean checkPrefix(MessageCreateEvent mce) {
        return mce.getMessage().getContent().get().startsWith(CommandUtil.getGuildPrefix(mce.getGuildId().get().asLong()));
    }

    private String trimCommand(MessageCreateEvent mce) {
        return mce.getMessage().getContent().get().substring(CommandUtil.getGuildPrefix(mce.getGuildId().get().asLong()).length());
    }

    private Mono<Void> processCommand(String trimmedCommand, MessageCreateEvent mce) {
        final String[] splittedCommand = trimmedCommand.split(" ", 2);
        return Flux.just(splittedCommand[0])
                .flatMap(CommandUtil::commandLookup)
                .flatMap(command -> command.executeCommand(mce, splittedCommand.length == 1 ? "":splittedCommand[1]))
                .then();
    }

}
