import React from "react";
import { TextField, TextFieldProps, InputAdornment } from "@mui/material";
import { Controller, useFormContext, FieldValues, Path } from "react-hook-form";

interface FormTextareaProps<T extends FieldValues>
  extends Omit<TextFieldProps, "name"> {
  name: Path<T>;
  label: string;
  rows?: number;
  maxRows?: number;
  maxLength?: number;
  showCharCount?: boolean;
  helperText?: string;
}

/**
 * Controlled textarea with character count, integrated with react-hook-form
 */
export function FormTextarea<T extends FieldValues>({
  name,
  label,
  rows = 4,
  maxRows,
  maxLength,
  showCharCount = false,
  helperText,
  ...props
}: FormTextareaProps<T>) {
  const {
    control,
    formState: { errors },
  } = useFormContext<T>();
  const error = errors[name];

  return (
    <Controller
      name={name}
      control={control}
      render={({ field }) => {
        const charCount = (field.value as string)?.length || 0;
        const remaining = maxLength ? maxLength - charCount : null;

        const charCountText =
          showCharCount && maxLength
            ? `${charCount}/${maxLength}`
            : showCharCount
            ? `${charCount} ตัวอักษร`
            : "";

        const helperTextDisplay =
          error?.message?.toString() ||
          (helperText
            ? `${helperText}${charCountText ? ` (${charCountText})` : ""}`
            : charCountText);

        return (
          <TextField
            {...field}
            {...props}
            label={label}
            multiline
            rows={rows}
            maxRows={maxRows}
            inputProps={{
              maxLength,
              ...props.inputProps,
            }}
            error={!!error || (remaining !== null && remaining < 0)}
            helperText={helperTextDisplay}
            fullWidth
          />
        );
      }}
    />
  );
}

interface FormCurrencyInputProps<T extends FieldValues>
  extends Omit<TextFieldProps, "name"> {
  name: Path<T>;
  label: string;
  currency?: string;
  helperText?: string;
  min?: number;
  max?: number;
}

/**
 * Currency input with Thai Baht formatting, integrated with react-hook-form
 */
export function FormCurrencyInput<T extends FieldValues>({
  name,
  label,
  currency = "฿",
  helperText,
  min,
  max,
  ...props
}: FormCurrencyInputProps<T>) {
  const {
    control,
    formState: { errors },
  } = useFormContext<T>();
  const error = errors[name];

  const formatCurrency = (value: number | string | undefined): string => {
    if (value === undefined || value === "" || value === null) return "";
    const num =
      typeof value === "string" ? parseFloat(value.replace(/,/g, "")) : value;
    if (isNaN(num)) return "";
    return num.toLocaleString("th-TH", { maximumFractionDigits: 2 });
  };

  const parseCurrency = (value: string): number | null => {
    const cleaned = value.replace(/,/g, "").replace(/[^0-9.]/g, "");
    if (!cleaned) return null;
    const num = parseFloat(cleaned);
    return isNaN(num) ? null : num;
  };

  return (
    <Controller
      name={name}
      control={control}
      render={({ field: { value, onChange, ...field } }) => (
        <TextField
          {...field}
          {...props}
          label={label}
          value={formatCurrency(value)}
          onChange={(e) => {
            const parsed = parseCurrency(e.target.value);
            onChange(parsed);
          }}
          error={!!error}
          helperText={error?.message?.toString() || helperText}
          fullWidth
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">{currency}</InputAdornment>
            ),
            ...props.InputProps,
          }}
          inputProps={{
            inputMode: "decimal",
            min,
            max,
            ...props.inputProps,
          }}
        />
      )}
    />
  );
}

interface FormNumberInputProps<T extends FieldValues>
  extends Omit<TextFieldProps, "name"> {
  name: Path<T>;
  label: string;
  min?: number;
  max?: number;
  step?: number;
  suffix?: string;
  prefix?: string;
  helperText?: string;
}

/**
 * Number input with optional formatting, integrated with react-hook-form
 */
export function FormNumberInput<T extends FieldValues>({
  name,
  label,
  min,
  max,
  step = 1,
  suffix,
  prefix,
  helperText,
  ...props
}: FormNumberInputProps<T>) {
  const {
    control,
    formState: { errors },
  } = useFormContext<T>();
  const error = errors[name];

  return (
    <Controller
      name={name}
      control={control}
      render={({ field: { value, onChange, ...field } }) => (
        <TextField
          {...field}
          {...props}
          type="number"
          label={label}
          value={value ?? ""}
          onChange={(e) => {
            const val = e.target.value;
            if (val === "") {
              onChange(null);
            } else {
              const num = parseFloat(val);
              onChange(isNaN(num) ? null : num);
            }
          }}
          error={!!error}
          helperText={error?.message?.toString() || helperText}
          fullWidth
          InputProps={{
            startAdornment: prefix ? (
              <InputAdornment position="start">{prefix}</InputAdornment>
            ) : undefined,
            endAdornment: suffix ? (
              <InputAdornment position="end">{suffix}</InputAdornment>
            ) : undefined,
            ...props.InputProps,
          }}
          inputProps={{
            min,
            max,
            step,
            ...props.inputProps,
          }}
        />
      )}
    />
  );
}

interface FormPercentageInputProps<T extends FieldValues>
  extends Omit<TextFieldProps, "name"> {
  name: Path<T>;
  label: string;
  helperText?: string;
  decimalPlaces?: number;
}

/**
 * Percentage input (stores value as decimal 0-1), integrated with react-hook-form
 */
export function FormPercentageInput<T extends FieldValues>({
  name,
  label,
  helperText,
  decimalPlaces = 2,
  ...props
}: FormPercentageInputProps<T>) {
  const {
    control,
    formState: { errors },
  } = useFormContext<T>();
  const error = errors[name];

  return (
    <Controller
      name={name}
      control={control}
      render={({ field: { value, onChange, ...field } }) => {
        // Convert stored decimal (0-1) to display percentage (0-100)
        const displayValue =
          value !== undefined && value !== null
            ? (Number(value) * 100).toFixed(decimalPlaces)
            : "";

        return (
          <TextField
            {...field}
            {...props}
            type="number"
            label={label}
            value={displayValue}
            onChange={(e) => {
              const val = e.target.value;
              if (val === "") {
                onChange(null);
              } else {
                const num = parseFloat(val);
                // Convert percentage (0-100) to decimal (0-1) for storage
                onChange(isNaN(num) ? null : num / 100);
              }
            }}
            error={!!error}
            helperText={error?.message?.toString() || helperText}
            fullWidth
            InputProps={{
              endAdornment: <InputAdornment position="end">%</InputAdornment>,
              ...props.InputProps,
            }}
            inputProps={{
              min: 0,
              max: 100,
              step: Math.pow(10, -decimalPlaces),
              ...props.inputProps,
            }}
          />
        );
      }}
    />
  );
}

export default FormTextarea;
