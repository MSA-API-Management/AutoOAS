package at.aau.serg.specgenerationsimple.models;

import java.time.Instant;
import java.time.LocalDateTime;

public class LocalTimeOrInstant {
    private Instant instant;
    private LocalDateTime local;

    public LocalTimeOrInstant(Instant instant, LocalDateTime local)
    {
        this.instant = instant;
        this.local = local;
    }

    public LocalTimeOrInstant(String value)
    {

    }
}
