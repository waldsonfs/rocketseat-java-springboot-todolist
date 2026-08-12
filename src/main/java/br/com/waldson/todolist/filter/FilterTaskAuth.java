package br.com.waldson.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.waldson.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter{

    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        if (requestURI != null && requestURI.startsWith("/tasks")) {

            // Pegar a autenticação
            String authorization = request.getHeader("Authorization");
            if (authorization == null || !authorization.startsWith("Basic ")) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                return;
            }

            String authEncoded = authorization.substring("Basic ".length()).trim();
            String authString;
            try {
                byte[] authDecode = Base64.getDecoder().decode(authEncoded);
                authString = new String(authDecode);
            } catch (IllegalArgumentException e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Authorization header");
                return;
            }

            String[] credential = authString.split(":", 2);
            if (credential.length != 2) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid credentials");
                return;
            }

            String username = credential[0];
            String password = credential[1];

            // Validar usuario
            if (this.userRepository == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "User repository not available");
                return;
            }

            var user = this.userRepository.findByUsername(username);
            if (user == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
                return;
            }

            // Validar senha
            var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
            if (passwordVerify.verified) {


                request.setAttribute("idUser",user.getId());

                filterChain.doFilter(request, response);
                return;
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid password");
                return;
            }

        } else {
            filterChain.doFilter(request, response);
        }
    }
}


