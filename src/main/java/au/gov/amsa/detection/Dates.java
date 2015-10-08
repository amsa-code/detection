package au.gov.amsa.detection;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Dates {

    public static final Date MAX = new Date(
            LocalDateTime.parse("2200-01-01 00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                    .atZone(ZoneOffset.UTC).toInstant().toEpochMilli());

    public static final Date MIN = new Date(
            LocalDateTime.parse("1900-01-01 00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                    .atZone(ZoneOffset.UTC).toInstant().toEpochMilli());
}
