package io.norberg.rut;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static io.norberg.rut.Router.Status.METHOD_NOT_ALLOWED;
import static io.norberg.rut.Router.Status.SUCCESS;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class RouterTest {

  @Rule public ExpectedException exception = ExpectedException.none();

  @Test
  public void testRouting() {
    final String getTarget = "target";
    final String postTarget = "target";

    final Router<String> router = Router.builder(String.class)
        .route("GET", "/foo/<bar>/baz", getTarget)
        .route("POST", "/foo/<bar>/baz", postTarget)
        .build();

    final Router.Result<String> result = router.result();

    final Router.Status status1 = router.route("GET", "/foo/bar-value/baz?q=a&w=b", result);
    assertThat(status1, is(Router.Status.SUCCESS));
    assertThat(result.status(), is(Router.Status.SUCCESS));
    assertThat(result.isSuccess(), is(true));
    assertThat(result.target(), is(getTarget));
    assertThat(result.queryStart(), is(19));
    assertThat(result.queryEnd(), is(26));
    assertThat(result.query().toString(), is("q=a&w=b"));
    assertThat(result.params(), is(1));
    assertThat(result.paramValueStart(0), is(5));
    assertThat(result.paramValueEnd(0), is(14));
    assertThat(result.allowedMethods(), hasSize(2));
    assertThat(result.allowedMethods(), containsInAnyOrder("GET", "POST"));
    final String name = result.paramName(0);
    final CharSequence value = result.paramValue(0);
    assertThat(name, is("bar"));
    assertThat(value.toString(), is("bar-value"));

    final Router.Status status2 = router.route("DELETE", "/foo/bar/baz", result);
    assertThat(status2, is(METHOD_NOT_ALLOWED));
    assertThat(result.status(), is(METHOD_NOT_ALLOWED));
    assertThat(result.allowedMethods(), hasSize(2));
    assertThat(result.allowedMethods(), containsInAnyOrder("GET", "POST"));
    assertThat(result.isSuccess(), is(false));

    final Router.Status status3 = router.route("GET", "/non/existent/path", result);
    assertThat(status3, is(Router.Status.NOT_FOUND));
    assertThat(result.status(), is(Router.Status.NOT_FOUND));
    assertThat(result.isSuccess(), is(false));
    try {
      result.allowedMethods();
      fail();
    } catch (IllegalStateException ignored) {}
  }

  @Test
  public void verifyResultTargetThrowsIfNotSuccessful() {
    final Router<String> router = Router.builder(String.class)
        .route("GET", "/foo", "foo")
        .build();

    final Router.Result<String> result = router.result();
    router.route("GET", "/bar", result);

    exception.expect(IllegalStateException.class);

    result.target();
  }


  @Test
  public void verifyResultParamNameThrowsIfNotSuccessful() {
    final Router<String> router = Router.builder(String.class)
        .route("GET", "/foo", "foo")
        .build();

    final Router.Result<String> result = router.result();
    router.route("GET", "/bar", result);

    exception.expect(IllegalStateException.class);

    result.paramName(0);
  }

  @Test
  public void testRouterBuilderWithoutClassArgument() {
    final Router.Builder<String> b = Router.builder();
    b.route("GET", "foo", "foo");
    b.build();
  }

  @Test
  public void verifyWrongMethodNotAllowed() {
    final Router<String> router = Router.builder(String.class)
        .route("GET", "/foo", "foo-get")
        .route("POST", "/foo", "foo-post")
        .build();
    final Router.Result<String> result = router.result();

    assertThat(router.route("PUT", "/foo", result), is(METHOD_NOT_ALLOWED));
    assertThat(result.status(), is(METHOD_NOT_ALLOWED));
    assertThat(result.allowedMethods(), hasSize(2));
    assertThat(result.allowedMethods(), containsInAnyOrder("GET", "POST"));

    assertThat(router.route("GGG", "/foo", result), is(METHOD_NOT_ALLOWED));
    assertThat(result.status(), is(METHOD_NOT_ALLOWED));
    assertThat(result.allowedMethods(), hasSize(2));
    assertThat(result.allowedMethods(), containsInAnyOrder("GET", "POST"));
  }

  @SuppressWarnings("RedundantStringConstructorCall")
  @Test
  public void verifyNonIdenticalStringMatches() {
    final Router<String> router = Router.builder(String.class)
        .route("GET", "/foo", "/foo")
        .build();
    final Router.Result<String> result = router.result();
    assertThat(router.route(String.valueOf(new char[]{'G', 'E', 'T'}),
                            String.valueOf(new char[]{'/', 'f', 'o', 'o'}), result),
               is(SUCCESS));
  }

}
