package security.starter;

import io.vertx.core.Future;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.handler.AuthenticationHandler;
import io.vertx.ext.web.handler.SimpleAuthenticationHandler;

public class FakeAuthn {

  static AuthenticationHandler create() {
    return SimpleAuthenticationHandler.create()
      .authenticate(ctx -> {
        String name = ctx.queryParams().get("name");

        if (name != null) {
          User user = User.fromName(name);
          String location = ctx.queryParams().get("location");
          if (location != null) {
            user.attributes().put("location", location);
          }
          return Future.succeededFuture(user);
        }

        return Future.failedFuture("No user");
      });
  }
}
