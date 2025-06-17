## [REST API](http://localhost:8080/doc)

## Концепция:

- Spring Modulith
    - [Spring Modulith: достигли ли мы зрелости модульности](https://habr.com/ru/post/701984/)
    - [Introducing Spring Modulith](https://spring.io/blog/2022/10/21/introducing-spring-modulith)
    - [Spring Modulith - Reference documentation](https://docs.spring.io/spring-modulith/docs/current-SNAPSHOT/reference/html/)

```
  url: jdbc:postgresql://localhost:5432/jira
  username: jira
  password: JiraRush
```

- Есть 2 общие таблицы, на которых не fk
    - _Reference_ - справочник. Связь делаем по _code_ (по id нельзя, тк id привязано к окружению-конкретной базе)
    - _UserBelong_ - привязка юзеров с типом (owner, lead, ...) к объекту (таска, проект, спринт, ...). FK вручную будем
      проверять

## Аналоги

- https://java-source.net/open-source/issue-trackers

## Тестирование

- https://habr.com/ru/articles/259055/

Список выполненных задач:
1. **Разобраться со структурой проекта (onboarding)**
2. **Удалить социальные сети: vk, yandex**
     * Удалены кнопки из шаблонов, связанные с данными социальными сетями
       - `resources/view/unauth/register.html`
       - `resources/view/login.html`
     * Удалены классы
       - `com.javarush.jira.login.internal.sociallogin.handler.YandexOAuth2UserDataHandler`
       - `com.javarush.jira.login.internal.sociallogin.handler.VkOAuth2UserDataHandler`
     * Удалена информация о данных социальных сетях в файле `application.yaml`
3. **Вынести чувствительную информацию в отдельный проперти файл**
     * Из файла application.yaml удалена чувствительная информация и перенесена во вновь созданный файл 
       application-secret.yaml 
       - логин БД
       - пароль БД
       - идентификаторы для OAuth регистрации/авторизации
       - настройки почты
     * Значения этих проперти будут считываться при старте сервера из переменных окружения машины 
       в виде ${VARIABLE_NAME:default_value}
     * В файле application.yaml создан импорт файла application-secret.yaml
