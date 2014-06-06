akka$1:::all-counters {
	printf("counter: %s -> %d", stringof(copyin(arg0, arg1 + 1)), arg2);
}

akka$1:::all-gauges {
	printf("gauge: %s -> %d", stringof(copyin(arg0, arg1 + 1)), arg2);
}