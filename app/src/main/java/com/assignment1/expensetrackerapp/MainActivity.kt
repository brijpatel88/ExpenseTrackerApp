package com.assignment1.expensetrackerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

// ---------------- THEME COLORS ----------------
private val Blue = Color(0xFF1976D2)
private val Green = Color(0xFF2E7D32)

private val BackgroundGradient = Brush.linearGradient(
    colors = listOf(Blue.copy(alpha = 0.6f), Green.copy(alpha = 0.6f)),
    start = Offset(0f, 0f),
    end = Offset(1000f, 1500f) // diagonal
)

// ---------------- CUSTOM TYPOGRAPHY (NO GOOGLE FONT) ----------------
private val AppTypography = Typography(
    titleLarge = Typography().titleLarge.copy(
        fontWeight = FontWeight.ExtraBold
    ),
    titleMedium = Typography().titleMedium.copy(
        fontWeight = FontWeight.Bold
    ),
    bodyLarge = Typography().bodyLarge.copy(
        fontWeight = FontWeight.Medium
    ),
    bodyMedium = Typography().bodyMedium.copy(
        fontWeight = FontWeight.Normal
    )
)

// ---------------- APP THEME ----------------
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Blue,
            onPrimary = Color.White,
            secondary = Green,
            background = Color.White,
            surface = Color.White
        ),
        typography = AppTypography,
        content = content
    )
}

// ---------------- DATA CLASS ----------------
data class Expense(
    val id: Int,
    val amount: Double,
    val note: String,
    val category: String
)

// ---------------- MAIN ACTIVITY ----------------
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                AppRoot()
            }
        }
    }
}

// ---------------- ROOT VIEW ----------------
@Composable
fun AppRoot() {
    var showChart by remember { mutableStateOf(false) }
    var expenses by remember { mutableStateOf(listOf<Expense>()) }
    var monthlyBudget by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGradient)
            .padding(12.dp)
    ) {
        if (showChart) {
            CategoryChartScreen(
                expenses = expenses,
                monthlyBudget = monthlyBudget.toDoubleOrNull() ?: 0.0,
                onBack = { showChart = false }
            )
        } else {
            ExpenseScreen(
                monthlyBudget = monthlyBudget,
                onBudgetChange = { monthlyBudget = it },
                expenses = expenses,
                onExpensesChange = { expenses = it },
                onShowChart = { showChart = true }
            )
        }
    }
}

// ---------------- EXPENSE SCREEN ----------------
@Composable
fun ExpenseScreen(
    monthlyBudget: String,
    onBudgetChange: (String) -> Unit,
    expenses: List<Expense>,
    onExpensesChange: (List<Expense>) -> Unit,
    onShowChart: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }
    var expanded by remember { mutableStateOf(false) }
    var editingId by remember { mutableStateOf<Int?>(null) }

    val categories = listOf("General", "Food", "Transport", "Bills", "Shopping", "Other")

    Scaffold(
        topBar = {
            Surface(color = Blue) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Expense Tracker",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White
                        )
                    )
                }
            }
        },
        containerColor = Color.Transparent
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
        ) {

            // ---- Budget Label ----
            Text(
                "Monthly Budget",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Green
                ),
                modifier = Modifier.padding(8.dp)
            )

            OutlinedTextField(
                value = monthlyBudget,
                onValueChange = onBudgetChange,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(0.dp) // square
            )

            Button(
                onClick = { onBudgetChange("") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Reset Monthly Budget", color = Color.White)
            }

            // ---- Calculations ----
            val total = expenses.sumOf { it.amount }
            val remaining = (monthlyBudget.toDoubleOrNull() ?: 0.0) - total

            Text(
                "Total Spent: $${"%.2f".format(total)}",
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                "Remaining: $${"%.2f".format(remaining)}",
                modifier = Modifier.padding(horizontal = 8.dp),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = if (remaining < 0) Color.Red else Green
            )

            Spacer(Modifier.height(16.dp))

            // ---- Add / Edit Expense ----
            Text(
                if (editingId == null) "Add New Expense" else "Edit Expense",
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Green
                )
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(0.dp)
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(0.dp)
            )

            Box(Modifier.padding(8.dp)) {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Text("Category: $category")
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                category = it
                                expanded = false
                            }
                        )
                    }
                }
            }

            // ---- Add / Save Button ----
            Button(
                onClick = {
                    val a = amount.toDoubleOrNull()
                    if (a != null) {
                        if (editingId == null) {
                            onExpensesChange(
                                expenses + Expense(
                                    id = (expenses.maxOfOrNull { it.id } ?: 0) + 1,
                                    amount = a,
                                    note = note,
                                    category = category
                                )
                            )
                        } else {
                            onExpensesChange(
                                expenses.map {
                                    if (it.id == editingId) it.copy(
                                        amount = a,
                                        note = note,
                                        category = category
                                    ) else it
                                }
                            )
                            editingId = null
                        }

                        amount = ""
                        note = ""
                        category = "General"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue)
            ) {
                Text(
                    if (editingId == null) "Add Expense" else "Save Changes",
                    color = Color.White
                )
            }

            // ---- View Chart ----
            OutlinedButton(
                onClick = onShowChart,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(0.dp)
            ) {
                Text("View Spending Chart")
            }

            // ---- Expense List ----
            LazyColumn(modifier = Modifier.padding(8.dp)) {
                items(expenses) { e ->
                    ExpenseCard(
                        e = e,
                        onEdit = {
                            editingId = e.id
                            amount = e.amount.toString()
                            note = e.note
                            category = e.category
                        },
                        onDelete = {
                            onExpensesChange(expenses.filter { it.id != e.id })
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

// ---------------- CARD COMPONENT ----------------
@Composable
fun ExpenseCard(
    e: Expense,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        elevation = CardDefaults.cardElevation(3.dp), // softer shadow
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "$${"%.2f".format(e.amount)}",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(e.category, color = Color.Gray)
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }

            if (e.note.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(e.note, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

// ---------------- CHART SCREEN ----------------
@Composable
fun CategoryChartScreen(
    expenses: List<Expense>,
    monthlyBudget: Double,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            Surface(color = Blue) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onBack) {
                        Text("Back", color = Color.White)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Spending Chart",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        },
        containerColor = Color.Transparent
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .padding(16.dp)
        ) {

            val totals = expenses.groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }

            val grandTotal = totals.values.sum()

            Text("Monthly Budget: $${"%.2f".format(monthlyBudget)}")
            Text("Total Spent: $${"%.2f".format(grandTotal)}")

            Spacer(Modifier.height(16.dp))

            if (grandTotal <= 0) {
                Text("No expenses yet.")
            } else {
                LazyColumn {
                    items(totals.toList()) { (category, total) ->
                        val percent = (total / grandTotal * 100).toFloat()

                        Column {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(category, fontWeight = FontWeight.Bold)
                                Text("${"%.1f".format(percent)}%")
                            }

                            Spacer(Modifier.height(4.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(18.dp)
                                    .background(Color.White.copy(alpha = 0.4f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(percent / 100f)
                                        .background(Blue)
                                )
                            }

                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}