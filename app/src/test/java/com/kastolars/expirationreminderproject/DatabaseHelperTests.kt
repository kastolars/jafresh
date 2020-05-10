package com.kastolars.expirationreminderproject

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.*

@Config(manifest = Config.NONE)
@RunWith(AndroidJUnit4::class)
class DatabaseHelperTests {

    private lateinit var dbHelper: DatabaseHelper
    private val timeZone = TimeZone.getDefault()

    @Before
    fun setup() {
        dbHelper = DatabaseHelper(getApplicationContext())
        val db = dbHelper.readableDatabase
        dbHelper.onUpgrade(db, 1, 2)
    }

    @Test
    fun testInsertItem(){
        // Arrange
        val uuid = UUID.randomUUID()
        val name = "milk"
        val expirationDate = Calendar.getInstance(timeZone).time
        val item = Item(uuid, name, expirationDate)
        // Act
        val id = dbHelper.insertItem(item)
        // Assert
        assert(id != -1L)
    }

    @Test
    fun testGetItemByUuid(){
        // Arrange
        val uuid = UUID.randomUUID()
        val name = "milk"
        val expirationDate = Calendar.getInstance(timeZone).time
        val item = Item(uuid, name, expirationDate)
        // Act
        val id = dbHelper.insertItem(item)
        val fetchedItem: Item? = dbHelper.fetchItemByUuid(uuid)
        // Assert
        assert(id != -1L)
        assert(fetchedItem != null)
        assert(fetchedItem == item)
    }

    @Test
    fun testUpdateItem(){
        // Arrange
        val uuid = UUID.randomUUID()
        val name = "milk"
        val expirationDate = Calendar.getInstance(timeZone).time
        val item = Item(uuid, name, expirationDate)

        val updatedItem = Item(uuid, "cheese", expirationDate)
        // Act
        val id = dbHelper.insertItem(item)
        val numRowsAffected = dbHelper.updateItem(updatedItem)
        val fetchedItem: Item? = dbHelper.fetchItemByUuid(uuid)
        // Assert
        assert(id != -1L)
        assert(numRowsAffected == 1)
        assert(fetchedItem != null)
        assert(fetchedItem == updatedItem)
    }

    @Test
    fun testDeleteItem() {
        // Arrange
        val uuid = UUID.randomUUID()
        val name = "milk"
        val expirationDate = Calendar.getInstance(timeZone).time
        val item = Item(uuid, name, expirationDate)

        // Act
        val id = dbHelper.insertItem(item)
        val numRowsAffected = dbHelper.deleteItem(item)
        val fetchedItem: Item? = dbHelper.fetchItemByUuid(item.uuid)
        // Assert
        assert(id != -1L)
        assert(numRowsAffected == 1)
        assert(fetchedItem == null)
    }

    @Test
    fun testFetchAllItems() {
        // Arrange
        val items = (0..8).map{
            val uuid = UUID.randomUUID()
            val name = "test"
            val expirationDate = Calendar.getInstance(timeZone).time
            val item = Item(uuid, name, expirationDate)
            dbHelper.insertItem(item)
            item
        }
        // Act
        val fetchedItems:ArrayList<Item> = dbHelper.fetchAllItems<Item>() as ArrayList<Item>
        // Assert
        items.forEach lit@{a ->
            fetchedItems.forEach{ b->
                if (a == b) return@lit
            }
            throw AssertionError("$a not found in list")
        }
    }
}