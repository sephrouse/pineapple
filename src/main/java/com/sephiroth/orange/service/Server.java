package com.sephiroth.orange.service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Server extends AbstractVerticle{
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    @Override
    public void start() {
        LOGGER.debug("service instance is " + config().getInteger("service.instance"));

        ClusterManager mgr = new HazelcastClusterManager();

        VertxOptions opt = new VertxOptions().setClusterManager(mgr);

        Vertx.clusteredVertx(opt, res -> {
            if (res.succeeded()) {
                Vertx vertx = res.result();

                DeploymentOptions webServiceVerticleOpt = new DeploymentOptions().setConfig(config()).setInstances(config().getInteger("service.instance", 2));

                vertx.deployVerticle("com.sephiroth.orange.service.web.WebServiceVerticle", webServiceVerticleOpt, res1 -> {
                    if(res1.succeeded()) {
                        System.out.println("deployment id is " + res1.result());
                        LOGGER.info("end of Server start." + Thread.currentThread().getName());
                        printProjectName();
                    }else{
                        System.out.println("failed to deploy " + res1.cause());
                        vertx.close();
                    }
                });

            }else{
                vertx.close();
            }
        });


    }

    private void printProjectName() {
        System.out.println("      ___           ___           ___           ___           ___           ___     ");
        System.out.println("     /\\  \\         /\\  \\         /\\  \\         /\\  \\         /\\__\\         /\\__\\    ");
        System.out.println("    /::\\  \\       /::\\  \\       /::\\  \\        \\:\\  \\       /:/ _/_       /:/ _/_   ");
        System.out.println("   /:/\\:\\  \\     /:/\\:\\__\\     /:/\\:\\  \\        \\:\\  \\     /:/ /\\  \\     /:/ /\\__\\  ");
        System.out.println("  /:/  \\:\\  \\   /:/ /:/  /    /:/ /::\\  \\   _____\\:\\  \\   /:/ /::\\  \\   /:/ /:/ _/_ ");
        System.out.println(" /:/__/ \\:\\__\\ /:/_/:/__/___ /:/_/:/\\:\\__\\ /::::::::\\__\\ /:/__\\/\\:\\__\\ /:/_/:/ /\\__\\");
        System.out.println(" \\:\\  \\ /:/  / \\:\\/:::::/  / \\:\\/:/  \\/__/ \\:\\~~\\~~\\/__/ \\:\\  \\ /:/  / \\:\\/:/ /:/  /");
        System.out.println("  \\:\\  /:/  /   \\::/~~/~~~~   \\::/__/       \\:\\  \\        \\:\\  /:/  /   \\::/_/:/  / ");
        System.out.println("   \\:\\/:/  /     \\:\\~~\\        \\:\\  \\        \\:\\  \\        \\:\\/:/  /     \\:\\/:/  /  ");
        System.out.println("    \\::/  /       \\:\\__\\        \\:\\__\\        \\:\\__\\        \\::/  /       \\::/  /   ");
        System.out.println("     \\/__/         \\/__/         \\/__/         \\/__/         \\/__/         \\/__/    ");
    }
}
