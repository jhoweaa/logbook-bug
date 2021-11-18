package com.example;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.http.netty.channel.ChannelPipelineCustomizer;
import org.zalando.logbook.Logbook;

import jakarta.inject.Singleton;
import org.zalando.logbook.netty.LogbookClientHandler;
import org.zalando.logbook.netty.LogbookServerHandler;

@Requires(beans = Logbook.class)
@Singleton
public class LogbookPipelineCustomizer implements BeanCreatedEventListener<ChannelPipelineCustomizer> {

  private final Logbook logbook;

  public LogbookPipelineCustomizer(Logbook logbook) {
    this.logbook = logbook;
  }

  @Override
  public ChannelPipelineCustomizer onCreated(BeanCreatedEvent<ChannelPipelineCustomizer> event) {
    ChannelPipelineCustomizer customizer = event.getBean();

    if (customizer.isServerChannel()) {
      customizer.doOnConnect(pipeline -> {
        pipeline.addAfter(
            ChannelPipelineCustomizer.HANDLER_HTTP_SERVER_CODEC,
            "logbook",
            new LogbookServerHandler(logbook)
        );
        return pipeline;
      });
    } else {
      customizer.doOnConnect(pipeline -> {
        pipeline.addAfter(
            ChannelPipelineCustomizer.HANDLER_HTTP_CLIENT_CODEC,
            "logbook",
            new LogbookClientHandler(logbook)
        );
        return pipeline;
      });
    }
    return customizer;
  }
}