import React from "react";
import {
  FormControl,
  FormControlLabel,
  FormHelperText,
  FormLabel,
  Checkbox,
  CheckboxProps,
  Switch,
  SwitchProps,
  Radio,
  RadioGroup,
  FormGroup,
} from "@mui/material";
import { Controller, useFormContext, FieldValues, Path } from "react-hook-form";

interface FormCheckboxProps<T extends FieldValues>
  extends Omit<CheckboxProps, "name"> {
  name: Path<T>;
  label: string;
  helperText?: string;
}

/**
 * Controlled checkbox integrated with react-hook-form
 */
export function FormCheckbox<T extends FieldValues>({
  name,
  label,
  helperText,
  ...props
}: FormCheckboxProps<T>) {
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
        <FormControl error={!!error}>
          <FormControlLabel
            control={
              <Checkbox
                {...field}
                {...props}
                checked={!!value}
                onChange={(e) => onChange(e.target.checked)}
              />
            }
            label={label}
          />
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

interface FormSwitchProps<T extends FieldValues>
  extends Omit<SwitchProps, "name"> {
  name: Path<T>;
  label: string;
  helperText?: string;
}

/**
 * Controlled switch integrated with react-hook-form
 */
export function FormSwitch<T extends FieldValues>({
  name,
  label,
  helperText,
  ...props
}: FormSwitchProps<T>) {
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
        <FormControl error={!!error}>
          <FormControlLabel
            control={
              <Switch
                {...field}
                {...props}
                checked={!!value}
                onChange={(e) => onChange(e.target.checked)}
              />
            }
            label={label}
          />
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

interface RadioOption {
  value: string | number;
  label: string;
  disabled?: boolean;
}

interface FormRadioGroupProps<T extends FieldValues> {
  name: Path<T>;
  label: string;
  options: RadioOption[];
  helperText?: string;
  row?: boolean;
}

/**
 * Controlled radio group integrated with react-hook-form
 */
export function FormRadioGroup<T extends FieldValues>({
  name,
  label,
  options,
  helperText,
  row = false,
}: FormRadioGroupProps<T>) {
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
        <FormControl error={!!error} component="fieldset">
          <FormLabel component="legend">{label}</FormLabel>
          <RadioGroup {...field} row={row}>
            {options.map((option) => (
              <FormControlLabel
                key={option.value}
                value={option.value}
                control={<Radio />}
                label={option.label}
                disabled={option.disabled}
              />
            ))}
          </RadioGroup>
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

interface CheckboxOption {
  value: string | number;
  label: string;
  disabled?: boolean;
}

interface FormCheckboxGroupProps<T extends FieldValues> {
  name: Path<T>;
  label: string;
  options: CheckboxOption[];
  helperText?: string;
  row?: boolean;
}

/**
 * Controlled checkbox group (multi-select) integrated with react-hook-form
 */
export function FormCheckboxGroup<T extends FieldValues>({
  name,
  label,
  options,
  helperText,
  row = false,
}: FormCheckboxGroupProps<T>) {
  const {
    control,
    formState: { errors },
  } = useFormContext<T>();
  const error = errors[name];

  return (
    <Controller
      name={name}
      control={control}
      render={({ field: { value = [], onChange } }) => (
        <FormControl error={!!error} component="fieldset">
          <FormLabel component="legend">{label}</FormLabel>
          <FormGroup row={row}>
            {options.map((option) => (
              <FormControlLabel
                key={option.value}
                control={
                  <Checkbox
                    checked={(value as (string | number)[]).includes(
                      option.value
                    )}
                    onChange={(e) => {
                      const currentValues = value as (string | number)[];
                      if (e.target.checked) {
                        onChange([...currentValues, option.value]);
                      } else {
                        onChange(
                          currentValues.filter((v) => v !== option.value)
                        );
                      }
                    }}
                    disabled={option.disabled}
                  />
                }
                label={option.label}
              />
            ))}
          </FormGroup>
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

export default FormCheckbox;
