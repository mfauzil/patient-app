import { DatePipe } from '@angular/common';
import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';
import { Patient } from '../../models/patient.model';
import { PatientService } from '../../services/patient.service';

@Component({
  selector: 'app-patient-list',
  imports: [FormsModule, RouterLink, DatePipe],
  templateUrl: './patient-list.html',
  styleUrl: './patient-list.css'
})
export class PatientList implements OnInit {

  private readonly service = inject(PatientService);
  private readonly destroyRef = inject(DestroyRef);

  readonly patients = signal<Patient[]>([]);
  readonly page = signal(0);
  readonly size = signal(10);
  readonly totalElements = signal(0);
  readonly totalPages = signal(0);
  readonly isFirst = signal(true);
  readonly isLast = signal(true);
  readonly keyword = signal('');
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly pageNumbers = computed(() =>
    Array.from({ length: this.totalPages() }, (_, i) => i));

  readonly rangeText = computed(() => {
    const total = this.totalElements();
    if (total === 0) return 'No data';
    const from = this.page() * this.size() + 1;
    const to = Math.min(from + this.patients().length - 1, total);
    return `Showing ${from}–${to} of ${total} patients`;
  });

  private readonly searchInput = new Subject<string>();

  ngOnInit(): void {
    this.searchInput
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe(value => {
        this.keyword.set(value);
        this.page.set(0);
        this.load();
      });

    this.load();
  }

  onSearchInput(value: string): void {
    this.searchInput.next(value);
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);

    this.service.list(this.keyword(), this.page(), this.size()).subscribe({
      next: res => {
        this.patients.set(res.content);
        this.totalElements.set(res.totalElements);
        this.totalPages.set(res.totalPages);
        this.isFirst.set(res.first);
        this.isLast.set(res.last);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load data. Make sure the backend is running.');
        this.patients.set([]);
        this.loading.set(false);
      }
    });
  }

  goToPage(p: number): void {
    if (p < 0 || p >= this.totalPages() || p === this.page()) return;
    this.page.set(p);
    this.load();
  }

  changeSize(value: string): void {
    this.size.set(Number(value));
    this.page.set(0);
    this.load();
  }

  remove(patient: Patient): void {
    const fullName = `${patient.firstName} ${patient.lastName}`;
    if (!confirm(`Delete patient ${patient.pid} - ${fullName}?`)) return;

    this.service.delete(patient.id!).subscribe({
      next: () => {
        // if this was the last row on the page, step back one page
        if (this.patients().length === 1 && this.page() > 0) {
          this.page.update(p => p - 1);
        }
        this.load();
      },
      error: () => this.error.set('Failed to delete patient.')
    });
  }
}