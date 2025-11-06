package iecompbot.annotations;

public @interface CommandAttribute {
    String command() default "";
    String description() default "";
}
