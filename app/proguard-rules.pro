# Room entities and Gson DTOs are kept by their annotations / generated adapters.
-keepattributes *Annotation*
-keep class com.daymark.taskflow.data.remote.RemoteTodo { *; }
-keep class com.daymark.taskflow.data.local.TaskEntity { *; }
