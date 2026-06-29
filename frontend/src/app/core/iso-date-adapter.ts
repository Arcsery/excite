import { Injectable } from '@angular/core';
import { NativeDateAdapter } from '@angular/material/core';

@Injectable()
export class IsoDateAdapter extends NativeDateAdapter {
  override parse(value: unknown): Date | null {
    if (typeof value === 'string') {
      const trimmedValue = value.trim();

      const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(trimmedValue);

      if (!match) {
        return null;
      }

      const year = Number(match[1]);
      const month = Number(match[2]);
      const day = Number(match[3]);

      const date = new Date(year, month - 1, day);

      const isValidDate =
        date.getFullYear() === year && date.getMonth() === month - 1 && date.getDate() === day;

      return isValidDate ? date : null;
    }

    return value instanceof Date ? value : null;
  }

  override format(date: Date): string {
    return this.toIsoDate(date);
  }

  private toIsoDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
  }
}

export const ISO_DATE_FORMATS = {
  parse: {
    dateInput: 'yyyy-MM-dd',
  },
  display: {
    dateInput: 'yyyy-MM-dd',
    monthYearLabel: 'yyyy MMM',
    dateA11yLabel: 'yyyy-MM-dd',
    monthYearA11yLabel: 'yyyy MMMM',
  },
};
