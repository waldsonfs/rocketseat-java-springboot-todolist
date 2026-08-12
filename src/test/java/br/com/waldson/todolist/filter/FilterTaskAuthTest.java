package br.com.waldson.todolist.filter;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.waldson.todolist.task.ITaskRepository;
import br.com.waldson.todolist.user.IUserRepository;
import br.com.waldson.todolist.user.UserModel;

@SpringBootTest
@AutoConfigureMockMvc
class FilterTaskAuthTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private ITaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void deveRetornarUnauthorizedQuandoNaoEnviarAuthorization() throws Exception {
        mockMvc.perform(post("/tasks/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskJsonValida()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornarUnauthorizedQuandoAuthorizationNaoForBasic() throws Exception {
        mockMvc.perform(post("/tasks/")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskJsonValida()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornarUnauthorizedQuandoBase64ForInvalido() throws Exception {
        mockMvc.perform(post("/tasks/")
                .header(HttpHeaders.AUTHORIZATION, "Basic ###")
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskJsonValida()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornarUnauthorizedQuandoUsuarioNaoExistir() throws Exception {
        mockMvc.perform(post("/tasks/")
                .header(HttpHeaders.AUTHORIZATION, basicAuth("nao-existe", "123456"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskJsonValida()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornarUnauthorizedQuandoSenhaForIncorreta() throws Exception {
        criarUsuario("waldson", "senha-correta");

        mockMvc.perform(post("/tasks/")
                .header(HttpHeaders.AUTHORIZATION, basicAuth("waldson", "senha-errada"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskJsonValida()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void devePermitirRequisicaoQuandoAutenticacaoForValida() throws Exception {
        criarUsuario("waldson", "senha-correta");

        mockMvc.perform(post("/tasks/")
                .header(HttpHeaders.AUTHORIZATION, basicAuth("waldson", "senha-correta"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskJsonValida()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.idUser", notNullValue()));
    }

    private void criarUsuario(String username, String password) {
        var user = new UserModel();
        user.setUsername(username);
        user.setName("Usuario teste");
        user.setPassword(BCrypt.withDefaults().hashToString(12, password.toCharArray()));
        userRepository.save(user);
    }

    private String basicAuth(String username, String password) {
        var token = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
    }

    private String taskJsonValida() {
        var startAt = LocalDateTime.now().plusDays(1);
        var endAt = LocalDateTime.now().plusDays(2);
        return """
                {
                  "title": "Estudar testes",
                  "description": "Criar testes com MockMvc",
                  "startAt": "%s",
                  "endAt": "%s",
                  "priority": "alta"
                }
                """.formatted(startAt, endAt);
    }
}
