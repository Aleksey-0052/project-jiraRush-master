package com.javarush.jira.profile.internal.web;

import com.javarush.jira.AbstractControllerTest;
import com.javarush.jira.common.util.JsonUtil;
import com.javarush.jira.profile.ProfileTo;
import com.javarush.jira.profile.internal.ProfileMapper;
import com.javarush.jira.profile.internal.ProfileRepository;
import com.javarush.jira.profile.internal.model.Profile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static com.javarush.jira.login.internal.web.UserTestData.*;
import static com.javarush.jira.login.internal.web.UserTestData.ADMIN_ID;
import static com.javarush.jira.login.internal.web.UserTestData.GUEST_ID;
import static com.javarush.jira.login.internal.web.UserTestData.GUEST_MAIL;
import static com.javarush.jira.login.internal.web.UserTestData.USER_ID;
import static com.javarush.jira.login.internal.web.UserTestData.USER_MAIL;
import static com.javarush.jira.profile.internal.web.ProfileTestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class ProfileRestControllerTest extends AbstractControllerTest {

    private static final String REST_URL = ProfileRestController.REST_URL;

    @Autowired
    ProfileMapper profileMapper;
    @Autowired
    private ProfileRepository profileRepository;


    /**
     * Аутентифицированный пользователь с ролью DEV получает свой профайл пользователя.
     */
    @Test
    @WithUserDetails(value = USER_MAIL)
    void getUserProfileTest_Success() throws Exception {
        USER_PROFILE_TO.setId(USER_ID);
        // Создаем ожидаемый профайл с идентификатором пользователя (1)

        perform(MockMvcRequestBuilders.get(REST_URL))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(print())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(PROFILE_TO_MATCHER.contentJson(USER_PROFILE_TO));
        // У статической переменной PROFILE_TO_MATCHER из класса ProfileTestData вызываем метод: public ResultMatcher
        // contentJson(T expected) и передаем в него ожидаемую модель. Данный метод сравнивает актуальную модель с
        // ожидаемой моделью.
    }


    /**
     * Аутентифицированный пользователь с ролью ADMIN получает свой профайл администратора.
     */
    @Test
    @WithUserDetails(value = ADMIN_MAIL)
    void getAdminProfileTest_Success() throws Exception {
        ADMIN_PROFILE_TO.setId(ADMIN_ID);
        // Создаем ожидаемый профайл с идентификатором пользователя (2)

        perform(MockMvcRequestBuilders.get(REST_URL))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(print())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(PROFILE_TO_MATCHER.contentJson(ADMIN_PROFILE_TO));
    }


    /**
     * Аутентифицированный пользователь, у которого в базе данных отсутствует профайл, получает пустой профиль.
     */
    @Test
    @WithUserDetails(value = GUEST_MAIL)
    void getGuestProfileTest_Success() throws Exception {
        GUEST_PROFILE_EMPTY_TO.setId(GUEST_ID);
        // Создаем ожидаемый профайл с идентификатором гостя (3)

        perform(MockMvcRequestBuilders.get(REST_URL))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(print())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(PROFILE_TO_MATCHER.contentJson(GUEST_PROFILE_EMPTY_TO));
        // У GUEST отсутствует профайл, профайл не будет найден в базе данных, поэтому будет создан новый пустой профайл,
        // в который будет записан идентификатор, извлеченный из <UserDetails>
    }


    /**
     * Обращение не аутентифицированного пользователя к своему профайлу возвращает статус "Unauthorized".
     */
    @Test
    void getProfileTest_Unauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }


    /**
     * Аутентифицированный пользователь успешно обновляет свой профайл.
     */
    @Test
    @WithUserDetails(USER_MAIL)
    void updateProfileTest_Success() throws Exception {
        ProfileTo updatedProfileTo = getUpdatedTo();
        // Создаем тестовый профайл для обновления существующего, id == null
        // Если в базе данных уже есть профайл с идентификатором, извлеченным из <UserDetails>, то найденный профайл
        // обновляется.

        perform(MockMvcRequestBuilders.put(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(updatedProfileTo)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        updatedProfileTo.setId(USER_ID);
        // Устанавливаем id ожидаемому профайлу
        Profile updated = profileRepository.getOrCreate(USER_ID);
        // Извлекаем из базы данных актуальный профайл (сущность)

        // Сравниваем актуальную модель и ожидаемую модель
        perform(MockMvcRequestBuilders.get(REST_URL)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(PROFILE_TO_MATCHER.contentJson(updatedProfileTo))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(USER_ID));

        PROFILE_MATCHER.assertMatch(updated, getUpdated(USER_ID));
        // Сравниваем актуальную сущность и ожидаемую сущность
    }


    /**
     * Аутентифицированный пользователь, у которого отсутствует в базе данных профайл, успешно сохраняет в базу
     * данных вновь созданный профайл.
     */
    @Test
    @WithUserDetails(value = GUEST_MAIL)
    public void createNewProfileTest_Success() throws Exception {
        ProfileTo createdProfileTo = getNewTo();
        // Создаем тестовый профайл и впервые сохраняем его в базу данных, id == null
        // Если в базе данных отсутствует профайл с идентификатором, извлеченным из <UserDetails>, то в новый
        // пустой профайл устанавливается идентификатор, извлеченный из <UserDetails>.

        perform(MockMvcRequestBuilders.put(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(createdProfileTo)))
                .andDo(print())
                .andExpect(status().isNoContent());

        createdProfileTo.setId(GUEST_ID);
        // Устанавливаем id в ожидаемый профайл

        Profile actualProfile = profileRepository.getExisted(GUEST_ID);
        // Извлекаем из базы данных по идентификатору пользователя его профайл (сущность)
        ProfileTo actualProfileTo = profileMapper.toTo(actualProfile);
        // Получаем актуальную модель

        // Сравниваем поля актуальной модели и поля ожидаемой модели
        assertAll(
                () -> assertEquals(actualProfileTo.id(), createdProfileTo.id()),
                () -> assertThat(actualProfileTo.getContacts()).hasSameElementsAs(createdProfileTo.getContacts()),
                () -> assertThat(actualProfileTo.getMailNotifications()).hasSameElementsAs((createdProfileTo.getMailNotifications()))
        );

        // Метод hasSameElementsAs() в классе org.assertj.core.api.Assertions проверяет, что два набора элементов
        // содержат одинаковые значения, независимо от их порядка.
    }


    /**
     * Попытка не аутентифицированного пользователя обновить свой профайл возвращает статус "Unauthorized".
     */
    @Test
    void updateProfileTest_Unauthorized() throws Exception {
        ProfileTo updatedProfileTo = getUpdatedTo();

        perform(MockMvcRequestBuilders.put(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(updatedProfileTo)))
                .andExpect(status().isUnauthorized());
    }


    /**
     * Попытка аутентифицированного пользователя обновить свой профайл и оставить некоторые поля пустыми возвращает
     * статус "Unprocessable Entity".
     */
    @Test
    @WithUserDetails(USER_MAIL)
    void whenUpdateProfileInvalidTo_thenReturnUnprocessableEntity() throws Exception {
        ProfileTo updatedProfileTo = getInvalidTo();

        perform(MockMvcRequestBuilders.put(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(updatedProfileTo)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }


    /**
     * Попытка аутентифицированного пользователя обновить свой профайл неизвестным контактом возвращает
     * статус "Unprocessable Entity".
     */
    @Test
    @WithUserDetails(USER_MAIL)
    void whenUpdateProfileWithUnknownContact_thenReturnUnprocessableEntity() throws Exception {
        ProfileTo updatedProfileTo = getWithUnknownContactTo();

        perform(MockMvcRequestBuilders.put(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(updatedProfileTo)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }


    /**
     * Попытка аутентифицированного пользователя обновить свой профайл неизвестными уведомлениями возвращает
     * статус "Unprocessable Entity".
     */
    @Test
    @WithUserDetails(USER_MAIL)
    void whenUpdateProfileWithUnknownNotification_thenReturnUnprocessableEntity() throws Exception {
        ProfileTo updatedProfileTo = getWithUnknownNotificationTo();

        perform(MockMvcRequestBuilders.put(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(updatedProfileTo)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }


    /**
     * Попытка аутентифицированного пользователя обновить свой профайл контактом, содержащим небезопасный HTML,
     * возвращает статус "Unprocessable Entity".
     */
    @Test
    @WithUserDetails(USER_MAIL)
    void whenUpdateProfileContactHtmlUnsafe_thenReturnUnprocessableEntity() throws Exception {
        ProfileTo updatedProfileTo = getWithContactHtmlUnsafeTo();

        perform(MockMvcRequestBuilders.put(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(updatedProfileTo)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

}

