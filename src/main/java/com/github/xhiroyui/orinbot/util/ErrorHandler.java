package com.github.xhiroyui.orinbot.util;

import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface ErrorHandler {
    public Mono<String> handle(Object object, Throwable error);
}
