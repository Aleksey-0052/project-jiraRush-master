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
4. **Переделать тесты так, чтоб во время тестов использовалась in memory БД (H2), а не PostgreSQL. Для этого нужно 
      определить 2 бина, и выборка какой из них использовать должно определяться активным профилем Spring. H2 не 
      поддерживает все фичи, которые есть у PostgreSQL, поэтому тебе придется немного упростить скрипты с тестовыми 
      данными**
      Чтобы не переделывать скрипты с тестовыми данными, решил использовать для тестирования приложения 
      библиотеку Testcontainers. В этих целях:
     * Добавил в файл pom.xml две зависимости: org.testcontainers:postgresql и org.testcontainers:junit-jupiter
     * В абстрактный класс BaseTests добавил аннотации @Testcontainers и @Container
     * Изменил файлы application.yaml application-test.yaml так, чтобы в реальной базе данных схема и данные создавались
       с помощью только Liquibase-скриптов, а в тестовой базе данных создание схемы осуществлялось на основании 
       Liquibase-скрипта из основного пакета, а данные - с помощью скрипта data.sql из тестового пакета.
     * С учетом этого в папке changelog был создан еще один файл с Liquibase-скриптом
5. **Написать тесты для всех публичных методов контроллера ProfileRestController. Хоть методов только 2, но тестовых 
     методов должно быть больше, так как нужно проверить success and unsuccess path**
6. **Сделать рефакторинг метода com.javarush.jira.bugtracking.attachment.FileUtil#upload, чтобы он использовал 
     современный подход для работы с файловой системой** 
7. **Добавить новый функционал: добавления тегов к задаче (REST API + реализация на сервисе). Фронт делать необязательно. 
     Таблица task_tag уже создана**
     * В связи с тем, что у сущности 'Task' над полем 'tags' в аннотации @ElementCollection параметр fetch имеет 
       значение FetchType.LAZY (режим ленивой загрузки для коллекции тегов), то в TaskRepository добавил метод по 
       получению из базы данных задачи с тегами с использованием в аннотации @Query оператора JOIN FETCH
     *  Добавил в классы TaskService и TaskController по три метода: добавление тега/тегов к задаче, получение тегов у 
        выбранной задачи и удаление тега у выбранной задачи  
8. **Добавить подсчет времени сколько задача находилась в работе и тестировании. Написать 2 метода на уровне сервиса, 
     которые параметром принимают задачу и возвращают затраченное время**
     * В конец скрипта инициализации базы данных '02-fill-data-database.sql' добавил 3 записи в таблицу 'activity'
     * В связи с тем, что у сущности 'Task' над полем 'activity' в аннотации @OneToMany параметр fetch имеет
       значение FetchType.LAZY (режим ленивой загрузки для списка активностей), добавил в ActivityRepository метод
       по получению из базы данных по идентификатору задачи списка активностей
     * Добавил необходимые методы в ActivityService и TaskController 
       