import React from "react";
import { Controller, useFormContext, FieldValues, Path } from "react-hook-form";
import { DatePicker, DatePickerProps } from "@mui/x-date-pickers/DatePicker";
import {
  DateTimePicker,
  DateTimePickerProps,
} from "@mui/x-date-pickers/DateTimePicker";
import { TimePicker, TimePickerProps } from "@mui/x-date-pickers/TimePicker";
import { FormControl, FormHelperText } from "@mui/material";
import dayjs, { Dayjs } from "dayjs";
import "dayjs/locale/th";

interface FormDatePickerProps<T extends FieldValues>
  extends Omit<DatePickerProps<Dayjs>, "value" | "onChange" | "name"> {
  name: Path<T>;
  label: string;
  helperText?: string;
  required?: boolean;
}

/**
 * Controlled date picker integrated with react-hook-form
 * Uses dayjs for date handling with Thai locale support
 */
export function FormDatePicker<T extends FieldValues>({
  name,
  label,
  helperText,
  required,
  ...props
}: FormDatePickerProps<T>) {
  const {
    control,
    formState: { errors },
  } = useFormContext<T>();
  const error = errors[name];

  return (
    <Controller
      name={name}
      control={control}
      render={({ field: { value, onChange, ref, ...field } }) => (
        <FormControl fullWidth error={!!error}>
          <DatePicker
            {...field}
            {...props}
            label={label}
            value={value ? dayjs(value) : null}
            onChange={(newValue) => {
              onChange(newValue?.toISOString() || null);
            }}
            slotProps={{
              textField: {
                inputRef: ref,
                error: !!error,
                helperText: error?.message?.toString() || helperText,
                required,
                fullWidth: true,
              },
              ...props.slotProps,
            }}
          />
        </FormControl>
      )}
    />
  );
}

interface FormDateTimePickerProps<T extends FieldValues>
  extends Omit<DateTimePickerProps<Dayjs>, "value" | "onChange" | "name"> {
  name: Path<T>;
  label: string;
  helperText?: string;
  required?: boolean;
}

/**
 * Controlled date-time picker integrated with react-hook-form
 */
export function FormDateTimePicker<T extends FieldValues>({
  name,
  label,
  helperText,
  required,
  ...props
}: FormDateTimePickerProps<T>) {
  const {
    control,
    formState: { errors },
  } = useFormContext<T>();
  const error = errors[name];

  return (
    <Controller
      name={name}
      control={control}
      render={({ field: { value, onChange, ref, ...field } }) => (
        <FormControl fullWidth error={!!error}>
          <DateTimePicker
            {...field}
            {...props}
            label={label}
            value={value ? dayjs(value) : null}
            onChange={(newValue) => {
              onChange(newValue?.toISOString() || null);
            }}
            slotProps={{
              textField: {
                inputRef: ref,
                error: !!error,
                helperText: error?.message?.toString() || helperText,
                required,
                fullWidth: true,
              },
              ...props.slotProps,
            }}
          />
        </FormControl>
      )}
    />
  );
}

interface FormTimePickerProps<T extends FieldValues>
  extends Omit<TimePickerProps<Dayjs>, "value" | "onChange" | "name"> {
  name: Path<T>;
  label: string;
  helperText?: string;
  required?: boolean;
}

/**
 * Controlled time picker integrated with react-hook-form
 */
export function FormTimePicker<T extends FieldValues>({
  name,
  label,
  helperText,
  required,
  ...props
}: FormTimePickerProps<T>) {
  const {
    control,
    formState: { errors },
  } = useFormContext<T>();
  const error = errors[name];

  return (
    <Controller
      name={name}
      control={control}
      render={({ field: { value, onChange, ref, ...field } }) => (
        <FormControl fullWidth error={!!error}>
          <TimePicker
            {...field}
            {...props}
            label={label}
            value={value ? dayjs(value) : null}
            onChange={(newValue) => {
              onChange(newValue?.toISOString() || null);
            }}
            slotProps={{
              textField: {
                inputRef: ref,
                error: !!error,
                helperText: error?.message?.toString() || helperText,
                required,
                fullWidth: true,
              },
              ...props.slotProps,
            }}
          />
        </FormControl>
      )}
    />
  );
}

interface FormDateRangePickerProps<T extends FieldValues> {
  startName: Path<T>;
  endName: Path<T>;
  startLabel: string;
  endLabel: string;
  helperText?: string;
  required?: boolean;
  minDate?: Dayjs;
  maxDate?: Dayjs;
  disabled?: boolean;
}

/**
 * Date range picker using two DatePicker components
 */
export function FormDateRangePicker<T extends FieldValues>({
  startName,
  endName,
  startLabel,
  endLabel,
  helperText,
  required,
  minDate,
  maxDate,
  disabled,
}: FormDateRangePickerProps<T>) {
  const {
    control,
    formState: { errors },
    watch,
  } = useFormContext<T>();
  const startError = errors[startName];
  const endError = errors[endName];
  const startValue = watch(startName);

  return (
    <FormControl fullWidth error={!!startError || !!endError}>
      <div style={{ display: "flex", gap: "16px" }}>
        <Controller
          name={startName}
          control={control}
          render={({ field: { value, onChange, ref, ...field } }) => (
            <DatePicker
              {...field}
              label={startLabel}
              value={value ? dayjs(value) : null}
              onChange={(newValue) => {
                onChange(newValue?.toISOString() || null);
              }}
              minDate={minDate}
              maxDate={maxDate}
              disabled={disabled}
              slotProps={{
                textField: {
                  inputRef: ref,
                  error: !!startError,
                  helperText: startError?.message?.toString(),
                  required,
                  fullWidth: true,
                },
              }}
            />
          )}
        />
        <Controller
          name={endName}
          control={control}
          render={({ field: { value, onChange, ref, ...field } }) => (
            <DatePicker
              {...field}
              label={endLabel}
              value={value ? dayjs(value) : null}
              onChange={(newValue) => {
                onChange(newValue?.toISOString() || null);
              }}
              minDate={startValue ? dayjs(startValue) : minDate}
              maxDate={maxDate}
              disabled={disabled}
              slotProps={{
                textField: {
                  inputRef: ref,
                  error: !!endError,
                  helperText: endError?.message?.toString(),
                  required,
                  fullWidth: true,
                },
              }}
            />
          )}
        />
      </div>
      {helperText && !startError && !endError && (
        <FormHelperText>{helperText}</FormHelperText>
      )}
    </FormControl>
  );
}

export default FormDatePicker;
