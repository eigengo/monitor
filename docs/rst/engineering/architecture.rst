############
Architecture
############

Let's explore Monitor's architecture. The driving principle is that the monitoring code should be
the *last man standing*; in other words, it is unacceptable for the monitoring code to bring down
the application being monitored.