package br.com.waldson.todolist.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.waldson.todolist.task.ITaskRepository;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

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
    void deveCriarUsuarioComSucesso() throws Exception {
        mockMvc.perform(post("/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "username": "waldson",
                          "name": "Waldson",
                          "password": "123456"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("waldson"))
                .andExpect(jsonPath("$.name").value("Waldson"));

        assertThat(userRepository.findByUsername("waldson")).isNotNull();
    }

    @Test
    void naoDeveCriarUsuarioDuplicado() throws Exception {
        criarUsuario("waldson", "123456");

        mockMvc.perform(post("/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "username": "waldson",
                          "name": "Outro nome",
                          "password": "abcdef"
                        }
                        """))
                .andExpect(status().isBadRequest());

        assertThat(userRepository.findAll()).hasSize(1);
    }

    @Test
    void deveCriptografarSenhaAntesDeSalvar() throws Exception {
        mockMvc.perform(post("/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "username": "ana",
                          "name": "Ana",
                          "password": "senha-secreta"
                        }
                        """))
                .andExpect(status().isCreated());

        var usuario = userRepository.findByUsername("ana");

        assertThat(usuario.getPassword()).isNotEqualTo("senha-secreta");
        assertThat(BCrypt.verifyer()
                .verify("senha-secreta".toCharArray(), usuario.getPassword())
                .verified).isTrue();
    }

    @Test
    void naoDeveCriarUsuarioComUsernameNulo() throws Exception {
        mockMvc.perform(post("/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "Sem username",
                          "password": "123456"
                        }
                        """))
                .andExpect(status().isBadRequest());

        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void naoDeveCriarUsuarioComUsernameVazio() throws Exception {
        mockMvc.perform(post("/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "username": "   ",
                          "name": "Username vazio",
                          "password": "123456"
                        }
                        """))
                .andExpect(status().isBadRequest());

        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void naoDeveCriarUsuarioComPasswordNula() throws Exception {
        mockMvc.perform(post("/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "username": "sem-senha",
                          "name": "Sem senha"
                        }
                        """))
                .andExpect(status().isBadRequest());

        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void naoDeveCriarUsuarioComPasswordVazia() throws Exception {
        mockMvc.perform(post("/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "username": "senha-vazia",
                          "name": "Senha vazia",
                          "password": ""
                        }
                        """))
                .andExpect(status().isBadRequest());

        assertThat(userRepository.findAll()).isEmpty();
    }

    private void criarUsuario(String username, String password) {
        var user = new UserModel();
        user.setUsername(username);
        user.setName("Usuario teste");
        user.setPassword(BCrypt.withDefaults().hashToString(12, password.toCharArray()));
        userRepository.save(user);
    }
}
