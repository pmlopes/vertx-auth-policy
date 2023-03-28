package security.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authorization.AuthorizationPolicyProvider;
import io.vertx.ext.auth.authorization.Policy;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.AuthorizationHandler;
import io.vertx.ext.web.handler.SimpleAuthenticationHandler;

public class MainVerticle extends AbstractVerticle {

  public static void main(String[] args) {
    Vertx.vertx()
      .deployVerticle(new MainVerticle())
      .onFailure(Throwable::printStackTrace);
  }


  @Override
  public void start(Promise<Void> started) {

    AuthorizationPolicyProvider authz = AuthorizationPolicyProvider.create();
    // load the policies from a bunch of json files
    vertx.fileSystem()
      .readDirBlocking("policies")
      .forEach(file -> {
        authz.addPolicy(new Policy(new JsonObject(vertx.fileSystem().readFileBlocking(file))));
      });

    Router app = Router.router(vertx);

    // Fake the authentication, let assume that all requests are authenticated for user "?user=Paulo"
    app.route()
      .handler(FakeAuthn.create());

    // from here is the real app code...
    app
      .post("/api")
      // this is the core of the policy enforcer (we could make this a top level handler, or apply to each operation)
      .handler(AuthorizationHandler.create().addAuthorizationProvider(authz))
      .handler(ctx -> {
        ctx.response()
          .putHeader("Location", "/api/1")
          .setStatusCode(201)
          .end();
      });

    app
      .get("/api/:id")
      // this is the core of the policy enforcer (we could make this a top level handler, or apply to each operation)
      .handler(AuthorizationHandler.create().addAuthorizationProvider(authz))
      .handler(ctx -> {
        ctx.response()
          .end("Read something @ /api/" + ctx.request().getParam("id"));
      });

    app
      .put("/api/:id")
      // this is the core of the policy enforcer (we could make this a top level handler, or apply to each operation)
      .handler(AuthorizationHandler.create().addAuthorizationProvider(authz))
      .handler(ctx -> {
        ctx.response()
          .end("Update something @ /api/" + ctx.request().getParam("id"));
      });

    app
      .delete("/api/:id")
      // this is the core of the policy enforcer (we could make this a top level handler, or apply to each operation)
      .handler(AuthorizationHandler.create().addAuthorizationProvider(authz))
      .handler(ctx -> {
        ctx.response()
          .setStatusCode(204)
          .end("Delete something @ /api/" + ctx.request().getParam("id"));
      });

    vertx.createHttpServer()
      .requestHandler(app)
      .listen(8888)
      .onSuccess(http -> {
        System.out.println("HTTP server started on port 8888");
      })
      .<Void>mapEmpty()
      .onComplete(started);
  }
}
