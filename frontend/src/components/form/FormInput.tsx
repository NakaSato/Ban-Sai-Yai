import React from "react";
import {
  TextField,
  TextFieldProps,
  FormControl,
  FormHelperText,
  InputLabel,
  InputAdornment,
  IconButton,
} from "@mui/material";
import { Visibility, VisibilityOff } from "@mui/icons-material";
import { Controller, useFormContext, FieldValues, Path } from "react-hook-form";

interface FormInputProps<T extends FieldValues>
  extends Omit<TextFieldProps, "name"> {
  name: Path<T>;
  label: string;
  helperText?: string;
}

/**
 * Controlled text input integrated with react-hook-form
 */
export function FormInput<T extends FieldValues>({
  name,
  label,
  helperText,
  ...props
}: FormInputProps<T>) {
  const {
    control,
    formState: { errors },
  } = useFormContext<T>();
  const error = errors[name];

  return (
    <Controller
      name={name}
      control={control}
      render={({ field }) => (
        <TextField
          {...field}
          {...props}
          label={label}
          error={!!error}
          helperText={error?.message?.toString() || helperText}
          fullWidth
          variant="outlined"
          InputLabelProps={{ shrink: true }}
        />
      )}
    />
  );
}

interface FormPasswordInputProps<T extends FieldValues>
  extends Omit<TextFieldProps, "name" | "type"> {
  name: Path<T>;
  label: string;
  helperText?: string;
}

/**
 * Password input with visibility toggle
 */
export function FormPasswordInput<T extends FieldValues>({
  name,
  label,
  helperText,
  ...props
}: FormPasswordInputProps<T>) {
  const [showPassword, setShowPassword] = React.useState(false);
  const {
    control,
    formState: { errors },
  } = useFormContext<T>();
  const error = errors[name];

  const handleClickShowPassword = () => setShowPassword((show) => !show);
  const handleMouseDownPassword = (
    event: React.MouseEvent<HTMLButtonElement>
  ) => {
    event.preventDefault();
  };

  return (
    <Controller
      name={name}
      control={control}
      render={({ field }) => (
        <TextField
          {...field}
          {...props}
          label={label}
          type={showPassword ? "text" : "password"}
          error={!!error}
          helperText={error?.message?.toString() || helperText}
          fullWidth
          variant="outlined"
          InputLabelProps={{ shrink: true }}
          InputProps={{
            endAdornment: (
              <InputAdornment position="end">
                <IconButton
                  aria-label={showPassword ? "hide password" : "show password"}
                  onClick={handleClickShowPassword}
                  onMouseDown={handleMouseDownPassword}
                  edge="end"
                >
                  {showPassword ? <VisibilityOff /> : <Visibility />}
                </IconButton>
              </InputAdornment>
            ),
          }}
        />
      )}
    />
  );
}

interface FormNumberInputProps<T extends FieldValues>
  extends Omit<TextFieldProps, "name" | "type"> {
  name: Path<T>;
  label: string;
  helperText?: string;
  min?: number;
  max?: number;
  prefix?: string;
  suffix?: string;
}

/**
 * Number input with optional prefix/suffix
 */
export function FormNumberInput<T extends FieldValues>({
  name,
  label,
  helperText,
  min,
  max,
  prefix,
  suffix,
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
      render={({ field: { onChange, ...field } }) => (
        <TextField
          {...field}
          {...props}
          label={label}
          type="number"
          error={!!error}
          helperText={error?.message?.toString() || helperText}
          fullWidth
          variant="outlined"
          InputLabelProps={{ shrink: true }}
          inputProps={{ min, max }}
          InputProps={{
            startAdornment: prefix ? (
              <InputAdornment position="start">{prefix}</InputAdornment>
            ) : undefined,
            endAdornment: suffix ? (
              <InputAdornment position="end">{suffix}</InputAdornment>
            ) : undefined,
          }}
          onChange={(e) => {
            const value = e.target.value === "" ? "" : Number(e.target.value);
            onChange(value);
          }}
        />
      )}
    />
  );
}

interface FormCurrencyInputProps<T extends FieldValues>
  extends Omit<TextFieldProps, "name" | "type"> {
  name: Path<T>;
  label: string;
  helperText?: string;
}

/**
 * Currency input formatted for Thai Baht
 */
export function FormCurrencyInput<T extends FieldValues>({
  name,
  label,
  helperText,
  ...props
}: FormCurrencyInputProps<T>) {
  const {
    control,
    formState: { errors },
  } = useFormContext<T>();
  const error = errors[name];

  return (
    <Controller
      name={name}
      control={control}
      render={({ field: { onChange, value, ...field } }) => (
        <TextField
          {...field}
          {...props}
          value={value || ""}
          label={label}
          type="number"
          error={!!error}
          helperText={error?.message?.toString() || helperText}
          fullWidth
          variant="outlined"
          InputLabelProps={{ shrink: true }}
          inputProps={{ min: 0, step: 0.01 }}
          InputProps={{
            startAdornment: <InputAdornment position="start">฿</InputAdornment>,
          }}
          onChange={(e) => {
            const value = e.target.value === "" ? "" : Number(e.target.value);
            onChange(value);
          }}
        />
      )}
    />
  );
}

export default FormInput;
