# Приложение трясометр

### Суть проекта
Мобильное приложение, позволяющее оценить качество дороги с использованием датчиков Android-устройства. Приложение берет данные по участку дороги, считает среднее из полученных данных датчика, выдает этому участку некий коэффициент, коэффициент отправляется на сервер. Само приложение берет данные участков в пределах экрана и отображает участки различными цветами с учетом полученных данных (нормальная, хорошая, плохая). 

### Дизайн
https://www.figma.com/file/jpmuuupfcZKXsq23PLuKBf/%D0%A2%D1%80%D1%8F%D1%81%D0%BE%D0%BC%D0%B5%D1%82%D1%80?node-id=0%3A1

## Структура проекта
### accelerometer
Черновик проекта. Состоит из одного экрана, которое показывает показания счетчиков в текущий момент + кнопки вкл/выкл. Работает в связке с TrackingService, но он находится в другой папке. Можно использовать как пример, чтобы разобраться что к чему. Реализован MVP, опять же можно ознакомиться с его работой. 
* AccelerometerActivityView - View, отображающая данные, полученные из сервиса при помощи BroadcastReciever. Данные получает одной единой строкой, а не кучей параметров
* AccelerometerPresenter - Presenter, его основная цель - запускать сервис. `<Context>` получает как контекст приложения, а не конкретной активити. Зачем и почему можно прочитать выше.
* AccelerometerPresenterMVP + AccelerometerView - интерфейсы, описывающие команды которыми общаются View и Presenter

### global
Содержит в себе компоненты необходимые для работы всего приложения, а не какой-то конкретной его части. Классы, связанные с `Application` необходимы для внедрения в необходимые модули контекста приложения, `NotificationHelper` необходим для запуска службы на Android, начиная с 8.0 (с этой версии сервис обязан уведомлять пользователя о том, что происходит его работа).

### models
Содержит в себе POJO-классы для грамотной работы с сетью. Не использовались и написаны на глаз, т.к. сервер не был доделан. Классы необходимые для того, чтобы получить от OverpassAPI секции: RoadsRequestBuilder + RoadsResponse. 

### network
Содержит классы для общения с серверами: корпоративным и Overpass. Первичная инициализация. 

### presenters
Содержит презентеры + интерфейсы презентеров всех экранов. StatisticsPresenter полностью функционирует, для других произведена первичная инициализация.

### ui
Поделен на activities и fragments. activities содержит все View-activity + их интерфейсы, fragments все View-фрагменты + их интерфейсы. Фрагмент StatisticsFragment реализован полностью, для других произведена первичная инициализация + базовая логика.

### Архитектура, применяемые паттерны
Приложение построено на основе паттерна MVP. В этом паттерне:
1. POJO класс = Model
2. View = Activity, Fragment
3. Presenter = Презентер - прослойка между View и Model

#### View отвечает за:
1. Создание экземпляра презентера и механизм его присоединения/отсоединения;
2. Оповещение презентера о важных для него событиях жизненного цикла;
3. Сообщение презентеру о входящих событиях;
4. Размещение вьюх и соединение их с данными;
5. Анимации;
6. Отслеживание событий;
7. Переход на другие экраны.

#### Presenter отвечает за:
1. Загрузку моделей;
2. Сохранение ссылки на модель и состояния представления;
3. Форматирование того, что должно быть отображено на экране, и указание представлению отобразить это;
4. Взаимодействие с репозиториями (база данных, сеть и т. д.) (прим. пер. Repository — это паттерн, на всякий случай);
5. Определение необходимых действий, когда получены входные события от представления.

Вся работа с асинхронными задачами уходит в Presenter. Вся бизнес-логика – в Presenter и Model. Activity, в свою очередь, становится View. Она начинает просто отображать то, что скажет Presenter и передаёт события в Presenter, чтобы тот решал, как быть дальше.

#### Итог:
По факту это значит, что практически ничего не программировать во View. В MVP общение с View идёт через вызовы команд. Для того, чтоб Presenter не превратился в God Object (аналог, God Activity - огромный класс с большим функционалом по сравнению с небольшими View и Model), нужно разделять каждый отдельный блок бизнес-логики в отдельный Presenter. В таком случае у получится много Presenter, но они будут очень простыми и понятными.

### Как работать с MVP
Шаблон:
1. Создать интерфейсы для View и Presenter
2. Классы View и Presenter реализуют эти интерфейсы
3. Класс View внутри создает экземпляр Presenter и передает себя в его конструктор
4. Класс Presenter содержит внутри поля интерфейса View и модели

#### Почему надо париться с интерфейсами?
В паттерне MVP презентер НЕ МОЖЕТ зависеть от Android. Как выяснено на практике, если код вашего презентера содержит код Android-фреймворка, а не только pure Java, вероятно, что-то идет не так. И соответственно, если представления нуждаются в ссылке на модель, видимо, вы делаете что-то неправильно. Как только возникнет вопрос тестов, большинство кода, который вам необходимо протестировать, будет в презентере. Что круто, так это то, что этому коду не нужен Android для запуска, так как у него есть только ссылки на интерфейс представления, а не на его реализацию в контексте Android. Это значит, что вы можете просто мокнуть интерфейс представления и написать чистые JUnit-тесты для бизнес-логики, проверяющие правильность вызова методов у мокнутого представления.

Но так или иначе презентер хранит внутри ссылку на View. Но здесь важен следующим момент: презентер содержит внутри поле ИНТЕРФЕЙСА View, а не поле конкретной активити/фрагмента. Т.е. презентер зависит от абстракции, а не от конкретики, т.о. реализуется принцип D из [SOLID](https://ru.wikipedia.org/wiki/SOLID_(%D0%BE%D0%B1%D1%8A%D0%B5%D0%BA%D1%82%D0%BD%D0%BE-%D0%BE%D1%80%D0%B8%D0%B5%D0%BD%D1%82%D0%B8%D1%80%D0%BE%D0%B2%D0%B0%D0%BD%D0%BD%D0%BE%D0%B5_%D0%BF%D1%80%D0%BE%D0%B3%D1%80%D0%B0%D0%BC%D0%BC%D0%B8%D1%80%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D0%B5))

#### Как использовать?
Например, использовать библиотеку Moxy или Mosby. Другой вариант, писать все базовые вещи самому. Moxy также позволяет работать вместе Dagger.
Пример использования Moxy:
1. Создать интерфейс View, наследуемый от MvpView
2. Создать интерфейс Presenter
3. Создать Presenter, наследуемый от MvpPresenter типа <интерфейс View> и интерфейса Presenter. Прописать @InjectViewState
4. Создать View, наследуемую от интерфейса View. Прописать @InjectPresenter
5. Пользоваться с удовольствием

#### Взаимодействие View и Presenter
View и Presenter общаются друг с другом через конкретные действия, не обращаясь к полям друг друга. Это еще одна задача интерфейсов. Например, произошло нажатие на кнопку, View обрабатывает нажатие посылая команду Presenter (Presenter.doSmth())-> Presenter выполняет какую-либо операцию и дает команду View (View.showToast("Hello")) -> View выполняет эту команду (в данном случае, показывает простой тост).

#### Как писать команды в интерфейсах?
1. Методы обновления View должны быть простыми и нацеленными на отдельный элемент. Это лучше, чем иметь один метод setMessage(Message message), который будет обновлять все, так как форматирование того, что надо отобразить, должно быть ответственностью презентера. Например, в будущем вы захотите отображать "Вы" вместо имени пользователя, если текущий пользователь является автором сообщения, а это является частью бизнес-логики.
2. Методы событий жизненного цикла презентера просты и не должны отображать истинный (переусложненный) системный жизненный цикл. Вы не обязаны обрабатывать какой бы то ни было из них. Но если хотите, чтобы презентер совершал какие-то действия на разных этапах этого цикла, можете обрабатывать в нем столько, сколько считаете нужным.
3. Входные события у презентера должны оставаться высокоуровневыми. Например, если хочется определять сложный жест, например, трехпальцевый свайп, это и другие события должны определяться View.
4. За бизнес-логику отвечает именно презентер, не нужно развязывать View руки.

### CI/CD
Концепция непрерывной интеграции и доставки (CI/CD) — основа тестирований. CI/CD — концепция, которая реализуется как конвейер, облегчая слияние только что закомиченного кода в основную кодовую базу. Концепция позволяет запускать различные типы тестов на каждом этапе (выполнение интеграционного аспекта) и завершать его запуском с развертыванием закомиченного кода в фактический продукт, который видят конечные пользователи (выполнение доставки).
CI/CD необходимы для разработки программного обеспечения с применением Agile-методологии, которая рекомендует использовать автоматическое тестирование для быстрой наладки рабочего программного обеспечения. Автоматическое тестирование дает заинтересованным лицам доступ к вновь созданным функциям и обеспечивает быструю обратную связь.
Простым языком: разработчик не должен беспокоиться о том, чтобы донести самую свежую версию приложения для тестировщиков. После коммита и пуша в мастер на CI-сервере приложение проходит через настроенные тесты (CI) и в случае успеха генерирует APK, который автоматически доставляется всем тестировщикам. Т.о. разработчик занимается лишь своей работой.

#### CircleCI
CircleCI - сервис, используемый для внедрения системы CI/CD. 
Для работы с ним необходимо:
1. Создать репозиторий на GitHub и инициализировать репозиторий в Android-проекте
2. Создать проект на [CircleCI](https://circleci.com/add-projects/gh/) и добавить к нему созданный репозиторий
3. Переключить в Android Studio отображение проекта на Project
4. Создать папку `<.circleci>` с файлом `<config.yml>` в ней
5. В `<config.yml>` прописать:
    version: 2
    jobs:
    build:
    working_directory: ~/code
    docker:
      - image: circleci/android:api-29
    environment:
      JVM_OPTS: -Xmx3200m
    steps:
      - checkout
      - restore_cache:
          key: jars-{{ checksum "build.gradle" }}-{{ checksum  "app/build.gradle" }}
      - run:
          name: Chmod permissions
          command: sudo chmod +x ./gradlew
      - run:
          name: Download Dependencies
          command: ./gradlew androidDependencies
      - save_cache:
          paths:
            - ~/.gradle
          key: jars-{{ checksum "build.gradle" }}-{{ checksum  "app/build.gradle" }}
      - store_artifacts:
          path: app/build/reports
          destination: reports
      - store_test_results:
          path: app/build/test-results
      - run:
          name: Initial build
          command: ./gradlew clean assembleRelease --no-daemon --stacktrace
      - store_artifacts:
          path: app/build/outputs/apk/
          destination: apks/

  deploy:
    working_directory: ~/code
    docker:
      - image: circleci/android:api-29
    environment:
      JVM_OPTS: -Xmx3200m
    steps:
      - checkout
      - restore_cache:
          key: jars-{{ checksum "build.gradle" }}-{{ checksum  "app/build.gradle" }}
      - run:
          name: Chmod permissions
          command: sudo chmod +x ./gradlew
      - run:
          name: Download Dependencies
          command: ./gradlew androidDependencies
      - save_cache:
          paths:
            - ~/.gradle
          key: jars-{{ checksum "build.gradle" }}-{{ checksum  "app/build.gradle" }}
      - store_artifacts:
          path: app/build/reports
          destination: reports
      - store_test_results:
          path: app/build/test-results
      - run:
          name: Initial build
          command: ./gradlew clean assembleRelease --no-daemon --stacktrace
      - store_artifacts:
          path: app/build/outputs/apk/
          destination: apks/
      - run:
          name: Send release apk
          command: ./gradlew assembleRelease crashlyticsUploadDistributionRelease

workflows:
  version: 2
  build-and-deploy:
    jobs:
      - build
      - deploy:
          requires:
            - build
          filters:
            branches:
              only:
                - master

Нужно следить за следующим моментом: в параметре `<docker: image>` должна быть прописана версия докера, соответствующая `<targetSdk>`.
Что написано в конфиге:
1. версия 2 (сильно отличается от 1 и гайды на 1 версию не подходят)
2. название этапа: build, deploy, ...
3. `<steps>` - этапы, выполняемые поочередно; запрос необходимых разрешений -> подгружаем зависимости `<gradle>` -> ... -> build релизного apk и созранение его в артефакты
4. `<deploy>` отличается от `<build>` тем, что он доставляет сформированный apk через сервис `<Beta>` до тестировщиков: `<./gradlew assembleRelease crashlyticsUploadDistributionRelease>`
5. workflow указывает на порядок выполнения действий: сначала билдим, потом деплоим

#### Что нужно добавить в проект, чтобы это заработало:
1. подключить к проекту CrashLytics от Fabric (можно при помощи плагина в студии)
2. сформировать подписанный apk
3. ввести ключи для работы с Fabric в `<fabric.properties>`
4. добавить проект в Beta

### Откуда брать участки дороги?
Использовать OpenStreetMaps. Есть несколько API, которые позволяют это делать. Один из них, это OverpassAPI. Информацию о нем легко найти.

#### Пример запроса на секции
С использованием Retrofit:

    RoadsRequestBuilder query = new RoadsRequestBuilder(30, new BoundingBox(50.746,7.154,50.748,7.157));
    NetworkService.getInstance().getOSMApi().getRoads(query.toString()).enqueue(new Callback<RoadsResponse>() {
        @Override
        public void onResponse(Call<RoadsResponse> call, Response<RoadsResponse> response) {
            Log.i("OSMResponse", String.valueOf(response.body().getElements()));
        }
        @Override
        public void onFailure(Call<RoadsResponse> call, Throwable t) {
            Log.i("OSMResponse", String.valueOf(t));
        }
    });

C использованием RxJava:

    NetworkService.getInstance().getOSMApi().getRoads(query.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(RoadsResponse -> Log.i("OSMResponse", String.valueOf(RoadsResponse)))
                .dispose()

#### Примеры запросов (проверить можно при помощи браузера)
* Получить секции в некотором радиусе:
    way["highway"~"primary|tertiary|residential|service"](around:350, 51.667251, 39.193426); 
out body geom;

* Получить секци по id:
    way(373634083); 
out body geom;

* Получить близжайщую секцию (но это как повезет, зависит от расстояния до дороги):
    way["highway"~"primary|tertiary|residential|service"](around:2, 51.668080, 39.191873); 
out body geom;

* Выдача секций в пределах экрана:
    [out:json]; 
way["highway"~"primary|tertiary|residential|service"](50.746,7.154,50.748,7.157); 
out body geom; 