import React from "react";
import {
  FormControl,
  FormHelperText,
  InputLabel,
  Select,
  MenuItem,
  SelectProps,
  Autocomplete,
  TextField,
  Chip,
} from "@mui/material";
import { Controller, useFormContext, FieldValues, Path } from "react-hook-form";

interface SelectOption {
  value: string | number;
  label: string;
  disabled?: boolean;
}

interface FormSelectProps<T extends FieldValues>
  extends Omit<SelectProps, "name"> {
  name: Path<T>;
  label: string;
  options: SelectOption[];
  helperText?: string;
}

/**
 * Controlled select input integrated with react-hook-form
 */
export function FormSelect<T extends FieldValues>({
  name,
  label,
  options,
  helperText,
  ...props
}: FormSelectProps<T>) {
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
        <FormControl fullWidth error={!!error} variant="outlined">
          <InputLabel id={`${name}-label`}>{label}</InputLabel>
          <Select {...field} {...props} labelId={`${name}-label`} label={label}>
            {options.map((option) => (
              <MenuItem
                key={option.value}
                value={option.value}
                disabled={option.disabled}
              >
                {option.label}
              </MenuItem>
            ))}
          </Select>
          {(error || helperText) && (
            <FormHelperText>
              {error?.message?.toString() || helperText}
            </FormHelperText>
          )}
        </FormControl>
      )}
    />
  );
}

interface FormAutocompleteProps<T extends FieldValues> {
  name: Path<T>;
  label: string;
  options: SelectOption[];
  helperText?: string;
  multiple?: boolean;
  freeSolo?: boolean;
  loading?: boolean;
  onInputChange?: (value: string) => void;
}

/**
 * Autocomplete/searchable select with react-hook-form
 */
export function FormAutocomplete<T extends FieldValues>({
  name,
  label,
  options,
  helperText,
  multiple = false,
  freeSolo = false,
  loading = false,
  onInputChange,
}: FormAutocompleteProps<T>) {
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
        <Autocomplete
          {...field}
          multiple={multiple}
          freeSolo={freeSolo}
          loading={loading}
          options={options}
          getOptionLabel={(option) => {
            if (typeof option === "string") return option;
            return option.label || "";
          }}
          isOptionEqualToValue={(option, val) => {
            if (typeof val === "string") return option.value === val;
            return option.value === val?.value;
          }}
          value={
            multiple
              ? options.filter((opt) =>
                  (value as (string | number)[])?.includes(opt.value)
                )
              : options.find((opt) => opt.value === value) || null
          }
          onChange={(_, newValue) => {
            if (multiple) {
              onChange(
                (newValue as SelectOption[])?.map((item) =>
                  typeof item === "string" ? item : item.value
                ) || []
              );
            } else {
              onChange(
                typeof newValue === "string"
                  ? newValue
                  : (newValue as SelectOption)?.value || ""
              );
            }
          }}
          onInputChange={(_, inputValue, reason) => {
            if (reason === "input" && onInputChange) {
              onInputChange(inputValue);
            }
          }}
          renderInput={(params) => (
            <TextField
              {...params}
              label={label}
              error={!!error}
              helperText={error?.message?.toString() || helperText}
              variant="outlined"
              InputLabelProps={{ shrink: true }}
            />
          )}
          renderTags={(tagValue, getTagProps) =>
            tagValue.map((option, index) => (
              <Chip
                label={typeof option === "string" ? option : option.label}
                {...getTagProps({ index })}
                key={typeof option === "string" ? option : option.value}
              />
            ))
          }
        />
      )}
    />
  );
}

export default FormSelect;
