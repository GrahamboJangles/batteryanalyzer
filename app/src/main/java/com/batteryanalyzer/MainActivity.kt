package com.batteryanalyzer

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var pieChart: PieChart
    private lateinit var statusTextView: TextView
    private lateinit var permissionButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pieChart = findViewById(R.id.pieChart)
        statusTextView = findViewById(R.id.statusTextView)
        permissionButton = findViewById(R.id.requestPermissionButton)

        permissionButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        setupPieChart()
        
        if (hasUsageStatsPermission()) {
            loadBatteryStats()
        } else {
            showPermissionRequired()
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasUsageStatsPermission()) {
            permissionButton.visibility = View.GONE
            loadBatteryStats()
        } else {
            showPermissionRequired()
        }
    }

    private fun setupPieChart() {
        pieChart.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            setExtraOffsets(5f, 10f, 5f, 5f)
            dragDecelerationFrictionCoef = 0.95f
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 58f
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            animateY(1400)
            legend.isEnabled = true
            legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            legend.orientation = Legend.LegendOrientation.HORIZONTAL
            legend.setDrawInside(false)
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(), packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun showPermissionRequired() {
        statusTextView.text = getString(R.string.no_permission)
        permissionButton.visibility = View.VISIBLE
    }

    private fun loadBatteryStats() {
        statusTextView.text = getString(R.string.loading)
        
        // Get battery usage data
        val usageStatsList = getUsageStatistics()
        
        if (usageStatsList.isEmpty()) {
            statusTextView.text = getString(R.string.no_data)
            return
        }
        
        // Calculate total usage
        val totalUsageTime = usageStatsList.sumOf { it.totalTimeInForeground }
        
        // Create entries for pie chart
        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()
        
        // Get top 10 apps by usage
        val topApps = usageStatsList
            .filter { it.totalTimeInForeground > 0 }
            .sortedByDescending { it.totalTimeInForeground }
            .take(10)
        
        // Create entries for each app
        for (usageStat in topApps) {
            val percentage = usageStat.totalTimeInForeground.toFloat() / totalUsageTime.toFloat() * 100f
            val appName = getAppName(usageStat.packageName)
            entries.add(PieEntry(percentage, appName))
            colors.add(ColorTemplate.MATERIAL_COLORS[entries.size % ColorTemplate.MATERIAL_COLORS.size])
        }
        
        // Create dataset
        val dataSet = PieDataSet(entries, "Battery Usage")
        dataSet.colors = colors
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f
        
        // Create pie data
        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(pieChart))
        data.setValueTextSize(11f)
        data.setValueTextColor(Color.BLACK)
        
        // Set data to chart
        pieChart.data = data
        pieChart.centerText = "Battery Usage\nby App"
        pieChart.invalidate() // refresh
        
        statusTextView.text = "Showing battery usage data for ${topApps.size} apps"
    }

    private fun getUsageStatistics(): List<UsageStats> {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        
        // Get data for the longest period possible
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.YEAR, -1) // Get data for up to 1 year
        val startTime = calendar.timeInMillis
        
        // Query for usage stats
        return usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_YEARLY, startTime, endTime)
            .filter { it.totalTimeInForeground > TimeUnit.MINUTES.toMillis(1) } // Filter out apps with minimal usage
    }

    private fun getAppName(packageName: String): String {
        val packageManager = packageManager
        return try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName // Return package name if app name not found
        }
    }
} 