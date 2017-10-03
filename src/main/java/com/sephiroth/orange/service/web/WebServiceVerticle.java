package com.sephiroth.orange.service.web;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class WebServiceVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebServiceVerticle.class);

    private static final long sockJSHeartbeatInterval = 5000;

    private static SockJSHandler sockJSHandler;

    private List<SockJSSocket> tempSoc = new ArrayList<>();

    @Override
    public void start(Future<Void> startFuture) {
        //LOGGER.info("start to web service verticle." + Thread.currentThread().getName());
        //LOGGER.info("try to get config." + config().getInteger("db.instance") + " " + config().getString("db.url"));



        Router router = Router.router(vertx);

        SockJSHandlerOptions sockJsHandlerOpt = new SockJSHandlerOptions().setHeartbeatInterval(sockJSHeartbeatInterval);
        //SockJSHandler sockJSHandler = SockJSHandler.create(vertx, sockJsHandlerOpt);
        sockJSHandler = SockJSHandler.create(vertx, sockJsHandlerOpt);
        //sockJSHandler.bridge(new BridgeOptions());
        //sockJSHandler.socketHandler(this::sctFunc);
        sockJSHandler.socketHandler(sockJSSocket -> {

//            vertx.sharedData().getCounter("counter", cntRes->{
//                cntRes.result().getAndIncrement(number ->{
//                    System.out.println("current number is " + number.result());
//
//                    vertx.sharedData().getClusterWideMap("sockMap", mapRes->{
//                        mapRes.result().put(number.result().toString(), sockJSSocket.writeHandlerID(), putRes -> {
//                            if (putRes.succeeded()) {
//                                System.out.println("put succeeds. " + sockJSSocket);
//                            }else{
//                                System.out.println("failed to put " + sockJSSocket);
//                            }
//                        });
//                    });
//                });
//
//            });

//            vertx.sharedData().getClusterWideMap("sockMap", mapRes->{
//                mapRes.result().put(sockJSSocket, sockJSSocket, putRes -> {
//                    if (putRes.succeeded()) {
//                        System.out.println("put succeeds. " + sockJSSocket);
//                    }else{
//                        System.out.println("failed to put " + sockJSSocket);
//                    }
//                });
//            });

            //tempSoc.add(sockJSSocket);

            vertx.sharedData().getCounter("counter", cntRes->{
                cntRes.result().getAndIncrement(number ->{
                    System.out.println("current number is " + number.result());

//                    vertx.sharedData().getClusterWideMap("sockMap", mapRes->{
//                        mapRes.result().put(number.result().toString(), sockJSSocket.writeHandlerID(), putRes -> {
//                            if (putRes.succeeded()) {
//                                System.out.println("put succeeds. " + sockJSSocket);
//                            }else{
//                                System.out.println("failed to put " + sockJSSocket);
//                            }
//                        });
//                    });

                    vertx.sharedData().getLocalMap("sockMap").put(number.result().toString(), sockJSSocket);
                });

            });

            System.out.println(sockJSSocket);
            //sockJSSocket.handler(sockJSSocket::write);
            //sockJSSocket.handler(this::whichThread);
            //sockJSSocket.write("This is " + Thread.currentThread().getName()).end();
            sockJSSocket.handler(data -> {
                System.out.println(data.toString() + ". id is " + sockJSSocket.writeHandlerID());
                sockJSSocket.write("This is " + Thread.currentThread().getName() + ". id is " + sockJSSocket.writeHandlerID());


            });

            sockJSSocket.endHandler(endHandler -> {
                System.out.println("close handler.  string " + sockJSSocket.writeHandlerID());
            });
        });

        router.route("/myapp/*").handler(sockJSHandler);
        router.get("/").handler(this::Index);
        router.route("/public/*").handler(StaticHandler.create("webroot/public"));
        router.route().handler(BodyHandler.create());
        //router.route().handler(sockJSHandler);
        router.get("/test/:writeid").handler(this::testWriteID);
        router.get("/test1/:socid").handler(this::test1SocID);
        router.get("/test2/:socid").handler(this::test2); // only for writeHandlerID
        router.get("/test3/:socid").handler(this::test3SocID);
        router.get("/test4/:socid").handler(this::test4SocID); // only for localmap

        vertx
                .createHttpServer()
                .requestHandler(router::accept)
                .listen(config().getInteger("http.port", 8080), res -> {
                    if (res.succeeded()) {
                        startFuture.complete();
                    }else{
                        startFuture.fail(res.cause());
                    }
                });
    }

    private void whichThread(Buffer b) {
        b.appendString(Thread.currentThread().getName());
    }

    private void Index(RoutingContext rc) {
        rc.response().sendFile("webroot/index.html");
    }

    private void testWriteID(RoutingContext rc) {
        String wID = rc.request().getParam("writeid");
        System.out.println("wID is " + wID);

//        for(SockJSSocket s : tempSoc) {
//            System.out.println("!!!! " + s);
//            //System.out.println("id is " + s.webSession().id());
//            //System.out.println("write id is " + s.writeHandlerID());
//            //System.out.println("data is " + s.webSession().data());
//            //s.end();
//            s.write("i am " + s);
//        }

        vertx.sharedData().getClusterWideMap("sockMap", mapRes->{
            mapRes.result().size(sizeRes -> {
                System.out.println("sock map size is " + sizeRes.result());
            });
        });



//        sockJSHandler.socketHandler(soc ->{
//            soc.
//        });

        rc.response().end("received wID is " + wID);
    }

    private void test1SocID(RoutingContext rc) {
        String socID = rc.request().getParam("socid");
        System.out.println("soc id is " + socID);

        EventBus eb = vertx.eventBus();

        vertx.sharedData().getClusterWideMap("sockMap", mapRes->{
            mapRes.result().get(socID, val -> {
//                SockJSSocket s = new SockJSSocket(val.result());
//
//                s.write("hey, this is " + s);
                //eb.send(val.result().toString(), "hey, this is " + val.result());
                System.out.println("hey ,this is " + val.result());
                eb.send(val.result().toString(), Buffer.buffer("java.lang.String cannot be cast to io.vertx.core.buffer.Buffer"));
                rc.response().end();
            });
        });
    }

    private void test2(RoutingContext rc) {
        String socID = rc.request().getParam("socid");
        System.out.println("soc id is " + socID);

        EventBus eb = vertx.eventBus();

        vertx.sharedData().getClusterWideMap("sockMap", mapRes->{
            mapRes.result().get(socID, val -> {

                System.out.println("hey ,this is " + val.result());
//                eb.send(val.result().toString(), Buffer.buffer("java.lang.String cannot be cast to io.vertx.core.buffer.Buffer"));
                eb.send(val.result().toString(), Buffer.buffer("close"));
                rc.response().end();
            });
        });
    }

    private void test3SocID(RoutingContext rc) {
        String socID = rc.request().getParam("socid");
        System.out.println("soc id is " + socID);

        vertx.sharedData().getClusterWideMap("sockMap", mapRes->{
            mapRes.result().get(socID, val -> {


                System.out.println("hey ,ready to close " + val.result());

                SockJSSocket s = (SockJSSocket) val.result();

                s.close();

                rc.response().end();
            });
        });
    }

    private void test4SocID(RoutingContext rc) {
        String socID = rc.request().getParam("socid");
        System.out.println("soc id is " + socID);

        SockJSSocket s = (SockJSSocket) vertx.sharedData().getLocalMap("sockMap").get(socID);
        s.write("hahaha i am back.");

        s.close();

        rc.response().end();
    }

    private void sctFunc(SockJSSocket s) {
        vertx.sharedData().getCounter("counter", cntRes->{
            cntRes.result().getAndIncrement(number ->{
                System.out.println("111111111111 current number is " + number.result());

                vertx.sharedData().getClusterWideMap("sockMap", mapRes->{
                    mapRes.result().put(number.result().toString(), SockJSSocket.class.cast(s), putRes -> {
                        if (putRes.succeeded()) {
                            System.out.println("put succeeds. " + s);
                        }else{
                            System.out.println("failed to put " + s);
                        }
                    });
                });
            });

        });

        System.out.println(s);
        //sockJSSocket.handler(sockJSSocket::write);
        //sockJSSocket.handler(this::whichThread);
        //sockJSSocket.write("This is " + Thread.currentThread().getName()).end();
        s.handler(data -> {
            System.out.println(data.toString() + ". id is " + s.writeHandlerID());
            s.write("This is " + Thread.currentThread().getName() + ". id is " + s.writeHandlerID());


        });

        s.endHandler(endHandler -> {
            System.out.println("close handler.  string " + s.writeHandlerID());
        });
    }
}
