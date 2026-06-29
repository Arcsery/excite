import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, ViewChild, computed, inject, signal } from '@angular/core';
import { FormBuilder, FormGroupDirective, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatOptionModule } from '@angular/material/core';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectChange, MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';

import { LeaveCalendarApiService } from '../../core/leave-calendar-api.service';
import { LeaveRequest, LeaveStatus, OnCallWeek, TeamMember } from '../../core/api.models';

type TeamMemberFilter = TeamMember | 'ALL';

@Component({
  selector: 'app-leave-calendar',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,

    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatOptionModule,
    MatProgressBarModule,
    MatSelectModule,
    MatSnackBarModule,
    MatTableModule,
    MatTooltipModule,
  ],
  templateUrl: './leave-calendar.html',
  styleUrl: './leave-calendar.scss',
})
export class LeaveCalendarComponent implements OnInit {
  @ViewChild('leaveFormDirective')
  private leaveFormDirective?: FormGroupDirective;

  private readonly api = inject(LeaveCalendarApiService);
  private readonly fb = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);

  readonly today = this.startOfToday();

  readonly teamMembers = signal<TeamMember[]>([]);
  readonly leaveRequests = signal<LeaveRequest[]>([]);
  readonly onCallWeeks = signal<OnCallWeek[]>([]);
  readonly loading = signal(false);
  readonly selectedTeamMember = signal<TeamMemberFilter>('ALL');

  readonly leaveStatuses: LeaveStatus[] = ['PENDING', 'APPROVED', 'REJECTED'];

  readonly displayedLeaveColumns = ['member', 'dates', 'reason', 'status', 'actions'];

  readonly displayedOnCallColumns = ['week', 'member', 'status', 'conflicts'];

  readonly approvedLeaves = computed(() =>
    this.leaveRequests().filter((leave) => leave.status === 'APPROVED'),
  );

  readonly pendingLeaves = computed(() =>
    this.leaveRequests().filter((leave) => leave.status === 'PENDING'),
  );

  readonly conflicts = computed(() => this.onCallWeeks().filter((week) => week.conflict));

  readonly leaveForm = this.fb.group({
    teamMember: this.fb.nonNullable.control<TeamMember | ''>('', Validators.required),
    startDate: this.fb.control<Date | null>(null, Validators.required),
    endDate: this.fb.control<Date | null>(null, Validators.required),
    reason: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(500)]),
  });

  readonly onCallForm = this.fb.group({
    from: this.fb.control<Date | null>(this.today, Validators.required),
    weeks: this.fb.nonNullable.control(8, [
      Validators.required,
      Validators.min(1),
      Validators.max(52),
    ]),
  });

  ngOnInit(): void {
    this.loadInitialData();
  }

  loadInitialData(): void {
    this.loading.set(true);

    this.api
      .getTeamMembers()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (members) => {
          this.teamMembers.set(members);
          this.loadLeaveRequests();
          this.loadOnCallSchedule();
        },
        error: (error: HttpErrorResponse) => {
          this.showError(this.getApiErrorMessage(error));
        },
      });
  }

  loadLeaveRequests(): void {
    this.loading.set(true);

    this.api
      .getLeaveRequests(this.selectedTeamMember())
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (requests) => this.leaveRequests.set(requests),
        error: (error: HttpErrorResponse) => {
          this.showError(this.getApiErrorMessage(error));
        },
      });
  }

  loadOnCallSchedule(): void {
    if (this.onCallForm.invalid) {
      this.onCallForm.markAllAsTouched();
      return;
    }

    const { from, weeks } = this.onCallForm.getRawValue();

    if (!from) {
      this.showError('From date is required.');
      return;
    }

    this.loading.set(true);

    this.api
      .getOnCallSchedule(this.toIsoDate(from), weeks)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (weeksResponse) => this.onCallWeeks.set(weeksResponse),
        error: (error: HttpErrorResponse) => {
          this.showError(this.getApiErrorMessage(error));
        },
      });
  }

  createLeaveRequest(): void {
    if (this.leaveForm.invalid) {
      this.leaveForm.markAllAsTouched();
      return;
    }

    const rawValue = this.leaveForm.getRawValue();

    if (!rawValue.teamMember || !rawValue.startDate || !rawValue.endDate) {
      this.showError('Please fill all required fields.');
      return;
    }

    if (rawValue.endDate < rawValue.startDate) {
      this.showError('End date cannot be before start date.');
      return;
    }

    this.loading.set(true);

    this.api
      .createLeaveRequest({
        teamMember: rawValue.teamMember,
        startDate: this.toIsoDate(rawValue.startDate),
        endDate: this.toIsoDate(rawValue.endDate),
        reason: rawValue.reason.trim(),
      })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: () => {
          this.leaveFormDirective?.resetForm({
            teamMember: '',
            startDate: null,
            endDate: null,
            reason: '',
          });

          this.leaveForm.markAsPristine();
          this.leaveForm.markAsUntouched();

          this.showSuccess('Leave request created.');
          this.loadLeaveRequests();
          this.loadOnCallSchedule();
        },
        error: (error: HttpErrorResponse) => {
          this.showError(this.getApiErrorMessage(error));
        },
      });
  }

  updateStatus(leaveRequest: LeaveRequest, status: LeaveStatus): void {
    if (leaveRequest.status === status) {
      return;
    }

    this.loading.set(true);

    this.api
      .updateLeaveStatus(leaveRequest.id, { status })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: () => {
          this.showSuccess('Leave status updated.');
          this.loadLeaveRequests();
          this.loadOnCallSchedule();
        },
        error: (error: HttpErrorResponse) => {
          this.showError(this.getApiErrorMessage(error));
        },
      });
  }

  changeTeamMemberFilter(value: TeamMemberFilter): void {
    this.selectedTeamMember.set(value);
    this.loadLeaveRequests();
  }

  changeTeamMemberFilterFromEvent(event: MatSelectChange): void {
    this.changeTeamMemberFilter(event.value as TeamMemberFilter);
  }

  updateStatusFromEvent(leaveRequest: LeaveRequest, event: MatSelectChange): void {
    this.updateStatus(leaveRequest, event.value as LeaveStatus);
  }

  getStatusClass(status: LeaveStatus): string {
    return `status-${status.toLowerCase()}`;
  }

  getConflictTooltip(week: OnCallWeek): string {
    if (!week.conflict) {
      return 'No conflict';
    }

    return `${week.onCallMemberName} has approved leave during this on-call week.`;
  }

  private toIsoDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
  }

  private startOfToday(): Date {
    const now = new Date();
    return new Date(now.getFullYear(), now.getMonth(), now.getDate());
  }

  private getApiErrorMessage(error: HttpErrorResponse): string {
    const body = error.error;

    if (!body) {
      return 'Unexpected error occurred.';
    }

    if (typeof body === 'string') {
      return body;
    }

    if (body.errors && typeof body.errors === 'object') {
      return Object.values(body.errors)
        .map((value) => String(value))
        .join(' ');
    }

    if (body.detail) {
      return body.detail;
    }

    if (body.title) {
      return body.title;
    }

    return 'Unexpected error occurred.';
  }

  private showSuccess(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 2500,
    });
  }

  private showError(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 4500,
    });
  }
}
