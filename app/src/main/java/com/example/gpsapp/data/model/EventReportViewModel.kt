package com.example.gpsapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gpsapp.data.model.EventReportItem
import com.example.gpsapp.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EventReportViewModel : ViewModel() {
    private val _eventReports = MutableStateFlow<List<EventReportItem>>(emptyList())
    val eventReports = _eventReports.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private var page = 1
    private var hasMoreData = true
    private var lastFetchParams = FetchParams()

    fun fetchEventReports(
        fromDate: String?,
        toDate: String?,
        vehicleNumber: String?,
        status: String?,
        reset: Boolean = false
    ) {
        val newParams = FetchParams(fromDate, toDate, vehicleNumber, status)

        if (reset || newParams != lastFetchParams) {
            _eventReports.value = emptyList()
            page = 1
            hasMoreData = true
            lastFetchParams = newParams
        }

        if (!hasMoreData || _isLoading.value) return

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val deviceIdSafe = vehicleNumber?.takeIf { it.isNotBlank() } ?: run {
                    _errorMessage.value = "Device ID is required"
                    _isLoading.value = false
                    return@launch
                }
                val fromSafe = fromDate?.takeIf { it.isNotBlank() } ?: run {
                    _errorMessage.value = "From date is required"
                    _isLoading.value = false
                    return@launch
                }
                val toSafe = toDate?.takeIf { it.isNotBlank() } ?: run {
                    _errorMessage.value = "To date is required"
                    _isLoading.value = false
                    return@launch
                }

                val response = RetrofitClient.apiService.getEventReport(
                    deviceId = deviceIdSafe,
                    from = fromSafe,
                    to = toSafe,
                    page = page,
                    size = 20
                )

                if (response.isSuccessful) {
                    val newData: List<EventReportItem> = response.body() ?: emptyList()
                    if (newData.isEmpty()) {
                        hasMoreData = false
                        if (page == 1) _errorMessage.value = "No data found"
                    } else {
                        _eventReports.value = _eventReports.value + newData
                        page++
                    }
                } else {
                    _errorMessage.value = "Error: ${response.code()} – ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Error fetching event report"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMoreData() {
        if (hasMoreData && !_isLoading.value) {
            fetchEventReports(
                fromDate = lastFetchParams.fromDate,
                toDate = lastFetchParams.toDate,
                vehicleNumber = lastFetchParams.vehicleNumber,
                status = lastFetchParams.status,
                reset = false
            )
        }
    }

    fun reset() {
        _eventReports.value = emptyList()
        _errorMessage.value = null
        _isLoading.value = false
        page = 1
        hasMoreData = true
        lastFetchParams = FetchParams()
    }

    data class FetchParams(
        val fromDate: String? = null,
        val toDate: String? = null,
        val vehicleNumber: String? = null,
        val status: String? = null
    )
}
