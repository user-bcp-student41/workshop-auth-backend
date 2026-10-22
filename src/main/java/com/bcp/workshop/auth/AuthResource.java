// Integrar en el AuthResource existente; conservar sus anotaciones de clase
// y el mecanismo vigente de autenticación de credenciales.
@Inject
AuthService authService;

@Inject
TokenService tokenService;

@POST
@Path("/login")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public Response login(LoginRequest request) {
    if (!LoginRequestValidator.isValid(request)) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new LoginError("Invalid login request"))
                .build();
    }

    // Este método debe conservar el acceso a datos y la verificación de
    // contraseña existentes. No se consulta ni modifica el esquema.
    UserRecord user = authService.authenticate(request.username, request.password);
    if (user == null) {
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(new LoginError("Invalid credentials"))
                .build();
    }

    TokenService.IssuedToken issued =
            tokenService.issue(user.username(), request.client);

    return Response.ok(new LoginResponse(
            issued.token(),
            issued.expiresIn(),
            LoginResponse.User.from(user))).build();
}

public record LoginError(String message) {}