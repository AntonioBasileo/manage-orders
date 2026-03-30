package it.manage.orders.utility;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericRecord;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.TimeZone;

@Slf4j
public class GenericAvroUtils {

  private GenericAvroUtils() {}

  public static Optional<Integer> getAsInteger(GenericRecord genericRecord, String fieldName) {
    var field = genericRecord.getSchema().getField(fieldName);

    if (field != null && genericRecord.get(field.name()) != null) {
      switch (extractTypeFromSchema(field.schema())) {
        case INT:
          return Optional.of((Integer) genericRecord.get(field.name()));

        case STRING:
          return Optional.of(Integer.valueOf(String.valueOf(genericRecord.get(field.name()))));

        default:
          log.warn(
              "GenericAvroUtils.getAsInteger for field: '{}' received unmanaged type: '{}'",
              fieldName,
              field.schema().getType());
          break;
      }
    }
    return Optional.empty();
  }

  public static Optional<String> getAsString(GenericRecord genericRecord, String fieldName) {
    var field = genericRecord.getSchema().getField(fieldName);

    if (field != null && genericRecord.get(field.name()) != null) {
      switch (extractTypeFromSchema(field.schema())) {
        case BOOLEAN:
        case INT:
        case LONG:
        case DOUBLE:
        case FLOAT:
        case STRING:
          return Optional.of(String.valueOf(genericRecord.get(field.name())));

        default:
          log.warn(
              "GenericAvroUtils.getAsString for field: '{}' received unmanaged type: '{}'",
              fieldName,
              field.schema().getType());
          break;
      }
    }

    return Optional.empty();
  }

  public static Optional<Boolean> getAsBoolean(GenericRecord genericRecord, String fieldName) {
    var field = genericRecord.getSchema().getField(fieldName);

    if (field != null && genericRecord.get(field.name()) != null) {
      switch (extractTypeFromSchema(field.schema())) {
        case BOOLEAN:
          return Optional.of((Boolean) genericRecord.get(field.name()));

        case INT:
          return Optional.of(
              (Integer) genericRecord.get(field.name()) == 0 ? Boolean.FALSE : Boolean.TRUE);

        case STRING:
          return Optional.of(Boolean.parseBoolean(String.valueOf(genericRecord.get(field.name()))));

        default:
          log.warn(
              "GenericAvroUtils.getAsBoolean for field: '{}' received unmanaged type: '{}'",
              fieldName,
              field.schema().getType());
          break;
      }
    }

    return Optional.empty();
  }

  public static Optional<LocalDate> getAsLocalDate(GenericRecord genericRecord, String fieldName) {
    var field = genericRecord.getSchema().getField(fieldName);

    if (field != null && genericRecord.get(field.name()) != null) {
      switch (extractTypeFromSchema(field.schema())) {
        case INT:
          return Optional.of(LocalDate.ofEpochDay((Integer) genericRecord.get(field.name())));

        case LONG:
          return Optional.of(
              LocalDate.ofInstant(
                  Instant.ofEpochMilli((Long) genericRecord.get(field.name())),
                  TimeZone.getDefault().toZoneId()));

        case STRING:
          return Optional.of(LocalDate.parse(String.valueOf(genericRecord.get(field.name()))));

        default:
          log.warn(
              "GenericAvroUtils.getAsLocalDate for field: '{}' received unmanaged type: '{}'",
              fieldName,
              field.schema().getType());
          break;
      }
    }

    return Optional.empty();
  }

  private static Type extractTypeFromSchema(Schema schema) {
    var type = schema.getType();
    if (Type.UNION.equals(type)) {
      for (Schema typeSchema : schema.getTypes()) {
        if (!Type.NULL.equals(typeSchema.getType())) type = typeSchema.getType();
      }
    }
    return type;
  }
}
