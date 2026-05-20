<img width="1148" height="101" alt="image" src="https://github.com/user-attachments/assets/b1079848-ebff-44ca-99c7-18154b7d46ad" />

# Контрольные вопросы

### 1. Типы данных в SharedPreferences
Примитивные: boolean, float, int, ... Объекты и кастомные классы напрямую нельзя (через JSON-сериализацию можно)

### 2. Метод для одного файла настроек SharedPreferences
getPreferences(Context.MODE_PRIVATE) - используется внутри Activity, создаёт файл с именем активити

### 3. Метод для нескольких файлов
getSharedPreferences(String name, int mode) - явное указание имени файла

### 4. Режимы доступа и самый надёжный
- MODE_PRIVATE - самый надёжный
- MODE_WORLD_READABLE/WRITEABLE - небезопасны
- MODE_MULTI_PROCESS - для процессов

### 5. Фрагменты кода SharedPreferences
```java
// Запись
SharedPreferences.Editor e = pref.edit();
e.putString("key", "value");
e.apply(); // commit() синхронный

// Чтение
String v = pref.getString("key", "default");

// Удаление
pref.edit().remove("key").apply();
```

### 6. Preferences DataStore: характеристики и типы
Характеристики: Асинхронный, типобезопасный, заменяет SharedPreferences
Типы:
- Preferences DataStore - пары ключ-значение
- Proto DataStore - структурированные данные с JSON/Protobuf схемой 
Использовать для любых настроек.

### 7. Код записи/чтения Preferences DataStore
```kotlin
// Запись
context.dataStore.edit { settings ->
  settings[stringPreferencesKey("key")] = "value"
}

// Чтение
val text: Flow<String> = context.dataStore.data.map { 
  it[stringPreferencesKey("key")] ?: "" 
}
```

### 8. Миграция SharedPreferences в DataStore
```kotlin
val dataStore = DataStoreFactory.create(
  serializer,
  produceFile = { context.preferencesDataStoreFile("settings") },
  migration = SharedPreferencesMigration(context, "old_prefs")
)
```
Проблемы: конфликт ключей, несовпадение типов, потеря данных
Решение: SharedPreferencesMigration с маппингом ключей, валидация в migrate()

### 9. Потокобезопасность DataStore vs SharedPreferences
- SharedPreferences: apply() асинх, но не атомарен, возможны race conditions  
- DataStore: Использует single-writer principle.
Проблемы миграции: Если писать в SP во время миграции - может быть потеря данных и диагностировать через StrictMode

### 10. Изолированные профили пользователей
Создавать файлы с уникальными именами: "user_${id}_prefs"
```kotlin
fun getDataStore(context: Context, userId: String) = 
  context.dataStore.preferencesDataStore(name = "user_$userId")
```
Переключение — смена ссылки на DataStore. Изоляция — разные файловые имена

### 11. Транзакционность в DataStore
Блок edit { } атомарен - все изменения применяются разом или откатываются
Проблемы: Нельзя откатить только часть
Решение: Разбить на несколько edit блоков если нужны части.

### 12. Преимущества Proto DataStore
- Производительность: Бинарный protobuf быстрее парсится и меньше весит чем XML.  
- Безопасность типов: Генерация классов из .proto.  
- Backward совместимость: Не переиспользовать номера полей, использовать reserved для удалённых полей, добавлять новые поля с новыми номерами.

### 13. Сложные настройки в Proto DataStore
```protobuf
message Settings {
  message NotificationConfig { bool enabled = 1; int32 priority = 2; }
  message DndSchedule { string start = 1; string end = 2; }
  
  NotificationConfig notifications = 1;
  repeated DndSchedule dnd = 2;
}
```
Serializer: Реализовать Serializer<T> с parseFrom.  
Частичное обновление: Через copy { }

### 14. SQLiteOpenHelper: onCreat/onUpgrade
- onCreate: Вызывается, если БД не существует. Создание таблиц CREATE TABLE
- onUpgrade: Вызывается, если oldVersion < newVersion

### 15. Managing пул соединений и WAL
- enableWriteAheadLogging() позволяет читать во время записи.  
- Exception SQLiteDatabaseLockedException: БД заблокирована другим потоком.  
- Стратегия: Короткие транзакции beginTransaction/endTransaction, синхронизация через synchronized

### 16. Raw SQL: TOP-10 артистов с AVG > 4 мин
```sql
SELECT a.name, COUNT(t.id) as track_count 
FROM artists a 
JOIN tracks t ON a.id = t.artist_id 
WHERE t.year = 2023 
GROUP BY a.id 
HAVING AVG(t.duration) > 240 
ORDER BY track_count DESC 
LIMIT 10;
```

### 17. Bulk insert 10,000 записей
```java
db.beginTransaction();
SQLiteStatement stmt = db.compileStatement("INSERT INTO table VALUES (?,?,?)");
for (Item item : list) {
  stmt.bindString(1, item.val1);
  stmt.bindLong(2, item.val2);
  stmt.executeInsert();
  stmt.clearBindings();
}
db.setTransactionSuccessful();
db.endTransaction();
```

### 18. Content Provider: IPC, CancellationSignal, Binder
- CancellationSignal: Передаётся в query(), позволяет отменить долгий запрос
- grantUriPermissions: Временное разрешение через Intent.FLAG_GRANT_READ_URI_PERMISSION
- Buffer limit: Большой Cursor (>1MB) вызовет TransactionTooLargeException.

### 19. applyBatch и транзакции в ContentProvider
applyBatch по умолчанию вызывает insert/update/delete в цикле без единой транзакции, неатомарно.  
Важно переопределить applyBatch и обернуть в db.beginTransaction(), тем самым обеспечит атомарность.

### 20. Content Provider между двумя приложениями
URI: content://com.example.logs.provider/logs.  
Разрешения: \<permission android:name="com.example.READ_LOGS" android:protectionLevel="signature"/\>.  
Реализация: query() возвращает Cursor, insert() пишет лог.  
Уведомления: getContext().getContentResolver().notifyChange(uri, null).

### 21. Безопасность Content Provider
- Signature permissions: \<permission android:protectionLevel="signature"/\> — только подписанные одним ключом приложения 
- Dynamic check: getCallingPackage() + checkCallingPermission() внутри query().  
- grantUriPermission(): Временный доступ context.grantUriPermission(receiver, uri, mode_flags).

### 22. Room: @Relation vs @Embedded
- \@Relation: One-to-Many, генерирует отдельный SELECT для дочерних
- \@Embedded: One-to-One
N+1 Problem: \@Relation делает 1 запрос на родителя + N на детей.  
Исправление: Вместо \@Relation использовать \@Query с JOIN и \@Embedded для дочернего объекта.

### 23. Room миграция со сложным преобразованием
Пример: разделение full_name на first_name/last_name.  
Шаги: Создать temp таблицу с новой схемой, INSERT INTO temp SELECT ..., DROP TABLE old, ALTER TABLE temp RENAME TO old  
Опасность fallbackToDestructiveMigration(): Удаляет старую БД с данными 
Тест: MigrationTestHelper - подаёт старую БД из assets, проверяет схему после миграции.

### 24. Когда raw queries быстрее Room?
- Сценарии: Очень сложный динамический SQL, который Room не поддерживает  
- Влияние: Room использует reflection на холодном старте
- Баланс: @RawQuery - позволяет писать SQL вручную, но возвращать типизированные объекты Room

### 25. Согласованность Room и Proto DataStore
Задача: Настройка сортировки влияет на запрос Room
Решение: Реактивная цепь через Kotlin Flow
```kotlin
val sortOrderFlow: Flow<String> = protoDataStore.data.map { it.sort }

val podcastsFlow: Flow<List<Podcast>> = sortOrderFlow.flatMapLatest { order ->
  roomDao.getPodcastsSorted(order) 
}
```
