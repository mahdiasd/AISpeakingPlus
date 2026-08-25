package ir.speaking.core.utils

import ir.speaking.admin.admin.db.AdminTable
import ir.speaking.core.network.utils.PrintHelper
import ir.speaking.feature.category.db.CategoryTable
import ir.speaking.feature.challenge.challenge.db.ChallengeTable
import ir.speaking.feature.challenge.progress.db.ChallengeProgressTable
import ir.speaking.feature.challenge.task.db.ChallengeTaskTable
import ir.speaking.feature.discount.db.DiscountCodeTable
import ir.speaking.feature.lightener.db.TranslationTable
import ir.speaking.feature.plan.db.PlanTable
import ir.speaking.feature.purchase.db.PurchaseTable
import ir.speaking.feature.scenario.progress.db.ScenarioProgressTable
import ir.speaking.feature.scenario.scenario.db.ScenarioTable
import ir.speaking.feature.scenario.task.db.ScenarioTaskTable
import ir.speaking.feature.user.db.UserTable
import ir.speaking.feature.user_device_info.db.UserDeviceInfoTable
import ir.speaking.feature.word.progress.db.WordProgressTable
import ir.speaking.feature.word.word.DailyWordTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseHelper {
    
    private val allTables = arrayOf(
        AdminTable,
        UserTable,
        CategoryTable,
        ScenarioTable,
        ScenarioTaskTable,
        ScenarioProgressTable,
        PurchaseTable,
        PlanTable,
        DiscountCodeTable,
        TranslationTable,
        DailyWordTable,
        WordProgressTable,
        ChallengeTable,
        ChallengeTaskTable,
        ChallengeProgressTable,
        UserDeviceInfoTable
    )
    
    /**
     * حذف کامل تمام جداول
     */
    fun dropAllTables() {
        try {
            PrintHelper.warning("Starting to drop all tables...")
            
            transaction {
                // ابتدا foreign key constraints رو غیرفعال می‌کنیم
                exec("SET session_replication_role = replica;")
                
                // حذف جداول به ترتیب معکوس (برای جلوگیری از مشکل foreign key)
                allTables.reversed().forEach { table ->
                    try {
                        SchemaUtils.drop(table)
                        PrintHelper.success("Dropped table: ${table.tableName}")
                    } catch (e: Exception) {
                        PrintHelper.error("Failed to drop table: ${table.tableName}", e)
                    }
                }
                
                // فعال کردن دوباره foreign key constraints
                exec("SET session_replication_role = DEFAULT;")
            }
            
            PrintHelper.success("All tables dropped successfully!")
            
        } catch (e: Exception) {
            PrintHelper.error("Failed to drop tables", e)
        }
    }
    
    /**
     * ایجاد مجدد تمام جداول
     */
    fun createAllTables() {
        try {
            PrintHelper.info("Creating all tables...")
            
            transaction {
                SchemaUtils.createMissingTablesAndColumns(*allTables)
            }
            
            PrintHelper.success("All tables created successfully!")
            
        } catch (e: Exception) {
            PrintHelper.error("Failed to create tables", e)
        }
    }
    
    /**
     * حذف و ایجاد مجدد تمام جداول (Reset کامل)
     */
    fun resetAllTables() {
        PrintHelper.info("Starting complete database reset...")
        dropAllTables()
        createAllTables()
        PrintHelper.success("Database reset completed!")
    }
    
    /**
     * پاک کردن داده‌های تمام جداول بدون حذف ساختار
     */
    fun clearAllData() {
        try {
            PrintHelper.warning("Clearing all data from tables...")
            
            transaction {
                // غیرفعال کردن foreign key constraints
                exec("SET session_replication_role = replica;")
                
                // پاک کردن داده‌ها به ترتیب معکوس
                allTables.reversed().forEach { table ->
                    try {
                        table.deleteAll()
                        PrintHelper.success("Cleared data from: ${table.tableName}")
                    } catch (e: Exception) {
                        PrintHelper.error("Failed to clear data from: ${table.tableName}", e)
                    }
                }
                
                // فعال کردن دوباره foreign key constraints
                exec("SET session_replication_role = DEFAULT;")
            }
            
            PrintHelper.success("All data cleared successfully!")
            
        } catch (e: Exception) {
            PrintHelper.error("Failed to clear data", e)
        }
    }
    
    /**
     * حذف جدول خاص
     */
    fun dropTable(table: Table) {
        try {
            PrintHelper.warning("Dropping table: ${table.tableName}")
            
            transaction {
                SchemaUtils.drop(table)
            }
            
            PrintHelper.success("Table ${table.tableName} dropped successfully!")
            
        } catch (e: Exception) {
            PrintHelper.error("Failed to drop table: ${table.tableName}", e)
        }
    }
    
    /**
     * ایجاد جدول خاص
     */
    fun createTable(table: Table) {
        try {
            PrintHelper.info("Creating table: ${table.tableName}")
            
            transaction {
                SchemaUtils.create(table)
            }
            
            PrintHelper.success("Table ${table.tableName} created successfully!")
            
        } catch (e: Exception) {
            PrintHelper.error("Failed to create table: ${table.tableName}", e)
        }
    }
    
    /**
     * نمایش لیست تمام جداول موجود در دیتابیس
     */
    fun showAllTables() {
        try {
            PrintHelper.info("Fetching all tables from database...")
            
            transaction {
                val tables = exec("SELECT tablename FROM pg_tables WHERE schemaname = 'public';") { rs ->
                    val tableList = mutableListOf<String>()
                    while (rs.next()) {
                        tableList.add(rs.getString("tablename"))
                    }
                    tableList
                }
                
                if (tables?.isNotEmpty() == true) {
                    PrintHelper.success("Tables in database:", tables.joinToString("\n• ", "• "))
                } else {
                    PrintHelper.info("No tables found in database")
                }
            }
            
        } catch (e: Exception) {
            PrintHelper.error("Failed to fetch tables", e)
        }
    }
    
    /**
     * بررسی وضعیت اتصال به دیتابیس
     */
    fun checkConnection() {
        try {
            PrintHelper.info("Checking database connection...")
            
            transaction {
                exec("SELECT 1;") { }
            }
            
            PrintHelper.success("Database connection is working!")
            
        } catch (e: Exception) {
            PrintHelper.error("Database connection failed", e)
        }
    }
}