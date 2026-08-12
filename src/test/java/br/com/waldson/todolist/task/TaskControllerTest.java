package br.com.waldson.todolist.task;

import static org.assertj.core.api.Assertions.assertThat;
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
import br.com.waldson.todolist.user.IUserRepository;
import br.com.waldson.todolist.user.UserModel;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private ITaskRepository taskRepository;

    private UserModel user;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        userRepository.deleteAll();
        user = criarUsuario("waldson", "123456");
    }

    @Test
    void deveCriarTarefaQuandoUsuarioEstiverAutenticado() throws Exception {
        mockMvc.perform(post("/tasks/")
                .header(HttpHeaders.AUTHORIZATION, basicAuth("waldson", "123456"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskJson("Estudar Spring", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title").value("Estudar Spring"));
    }

    @Test
    void naoDeveCriarTarefaSemAutenticacao() throws Exception {
        mockMvc.perform(post("/tasks/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskJson("Sem auth", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))))
                .andExpect(status().isUnauthorized());

        assertThat(taskRepository.findAll()).isEmpty();
    }

    @Test
    void devePreencherIdUserComUsuarioAutenticado() throws Exception {
        mockMvc.perform(post("/tasks/")
                .header(HttpHeaders.AUTHORIZATION, basicAuth("waldson", "123456"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskJson("Com dono", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUser").value(user.getId().toString()));
    }

    @Test
    void devePersistirTarefaCorretamente() throws Exception {
        mockMvc.perform(post("/tasks/")
                .header(HttpHeaders.AUTHORIZATION, basicAuth("waldson", "123456"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskJson("Persistir", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))))
                .andExpect(status().isOk());

        var tarefas = taskRepository.findAll();

        assertThat(tarefas).hasSize(1);
        assertThat(tarefas.get(0).getTitle()).isEqualTo("Persistir");
        assertThat(tarefas.get(0).getDescription()).isEqualTo("Descricao da tarefa");
        assertThat(tarefas.get(0).getPriority()).isEqualTo("alta");
        assertThat(tarefas.get(0).getIdUser()).isEqualTo(user.getId());
    }

    @Test
    void naoDeveCriarTarefaComTituloInvalido() throws Exception {
        mockMvc.perform(post("/tasks/")
                .header(HttpHeaders.AUTHORIZATION, basicAuth("waldson", "123456"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskJson("   ", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))))
                .andExpect(status().isBadRequest());

        assertThat(taskRepository.findAll()).isEmpty();
    }

    @Test
    void naoDeveCriarTarefaComDatasNoPassado() throws Exception {
        mockMvc.perform(post("/tasks/")
                .header(HttpHeaders.AUTHORIZATION, basicAuth("waldson", "123456"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskJson("Datas passadas", LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1))))
                .andExpect(status().isBadRequest());

        assertThat(taskRepository.findAll()).isEmpty();
    }

    @Test
    void naoDeveCriarTarefaComDataInicioMaiorQueDataTermino() throws Exception {
        mockMvc.perform(post("/tasks/")
                .header(HttpHeaders.AUTHORIZATION, basicAuth("waldson", "123456"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskJson("Datas invertidas", LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(1))))
                .andExpect(status().isBadRequest());

        assertThat(taskRepository.findAll()).isEmpty();
    }

    private UserModel criarUsuario(String username, String password) {
        var userModel = new UserModel();
        userModel.setUsername(username);
        userModel.setName("Usuario teste");
        userModel.setPassword(BCrypt.withDefaults().hashToString(12, password.toCharArray()));
        return userRepository.save(userModel);
    }

    private String basicAuth(String username, String password) {
        var token = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
    }

    private String taskJson(String title, LocalDateTime startAt, LocalDateTime endAt) {
        return """
                {
                  "title": "%s",
                  "description": "Descricao da tarefa",
                  "startAt": "%s",
                  "endAt": "%s",
                  "priority": "alta"
                }
                """.formatted(title, startAt, endAt);
    }
}
