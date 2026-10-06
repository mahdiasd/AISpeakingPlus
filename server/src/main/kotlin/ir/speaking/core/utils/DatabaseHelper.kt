package ir.speaking.core.utils

import ir.speaking.core.network.utils.PrintHelper
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseHelper {
    
    private val allTables = emptyArray<Table>()
    
    fun dropAllTables() {
        try {
            PrintHelper.warning("Starting to drop all tables...")
            
            transaction {
                exec("SET session_replication_role = replica;")
                allTables.reversed().forEach { table ->
                    try {
                        SchemaUtils.drop(table)
                        PrintHelper.success("Dropped table: ${table.tableName}")
                    } catch (e: Exception) {
                        PrintHelper.error("Failed to drop table: ${table.tableName}", e)
                    }
                }
                exec("SET session_replication_role = DEFAULT;")
            }
            
            PrintHelper.success("All tables dropped successfully!")
        } catch (e: Exception) {
            PrintHelper.error("Failed to drop tables", e)
        }
    }
    
    fun createAllTables() {
        try {
            PrintHelper.info("Creating all tables...")
            if (allTables.isNotEmpty()) {
                transaction {
                    SchemaUtils.createMissingTablesAndColumns(*allTables)
                }
            }
            PrintHelper.success("All tables created successfully!")
        } catch (e: Exception) {
            PrintHelper.error("Failed to create tables", e)
        }
    }
    
    fun resetAllTables() {
        PrintHelper.info("Starting complete database reset...")
        dropAllTables()
        createAllTables()
        PrintHelper.success("Database reset completed!")
    }
    
    fun clearAllData() {
        try {
            PrintHelper.warning("Clearing all data from tables...")
            transaction {
                exec("SET session_replication_role = replica;")
                allTables.reversed().forEach { table ->
                    try {
                        table.deleteAll()
                        PrintHelper.success("Cleared data from: ${table.tableName}")
                    } catch (e: Exception) {
                        PrintHelper.error("Failed to clear data from: ${table.tableName}", e)
                    }
                }
                exec("SET session_replication_role = DEFAULT;")
            }
            PrintHelper.success("All data cleared successfully!")
        } catch (e: Exception) {
            PrintHelper.error("Failed to clear data", e)
        }
    }
    
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