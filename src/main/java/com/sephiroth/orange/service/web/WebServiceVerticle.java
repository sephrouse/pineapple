package com.sephiroth.orange.service.web;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebServiceVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebServiceVerticle.class);

    @Override
    public void start(Future<Void> startFuture) {
        LOGGER.info("start to web service verticle." + Thread.currentThread().getName());
        LOGGER.info("try to get config." + config().getInteger("db.instance") + " " + config().getString("db.url"));

        startFuture.complete();
    }
}
