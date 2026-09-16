import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import {
    AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { GENDERS, Patient, STATES } from '../../models/patient.model';
import { PatientService } from '../../services/patient.service';

/** Mirrors @Past on the backend. */
function pastDate(control: AbstractControl): ValidationErrors | null {
    if (!control.value) return null;
    return new Date(control.value) < new Date() ? null : { pastDate: true };
}

@Component({
    selector: 'app-patient-form',
    imports: [ReactiveFormsModule, RouterLink],
    templateUrl: './patient-form.html',
    styleUrl: './patient-form.css'
})
export class PatientForm implements OnInit {

    private readonly fb = inject(FormBuilder);
    private readonly service = inject(PatientService);
    private readonly route = inject(ActivatedRoute);
    private readonly router = inject(Router);

    readonly genders = GENDERS;
    readonly states = STATES;

    readonly id = signal<number | null>(null);
    readonly saving = signal(false);
    readonly loadError = signal<string | null>(null);
    readonly formError = signal<string | null>(null);

    readonly form = this.fb.group({
        pid: ['', [Validators.required, Validators.maxLength(20)]],
        firstName: ['', [Validators.required, Validators.maxLength(60)]],
        lastName: ['', [Validators.required, Validators.maxLength(60)]],
        dateOfBirth: ['', [Validators.required, pastDate]],
        gender: ['MALE', Validators.required],
        phoneNo: ['', [Validators.required, Validators.pattern(/^[0-9 +()-]{6,20}$/)]],
        address: this.fb.group({
            addressLine: ['', [Validators.required, Validators.maxLength(200)]],
            suburb: ['', [Validators.required, Validators.maxLength(100)]],
            state: ['NSW', Validators.required],
            postcode: ['', [Validators.required, Validators.pattern(/^\d{4}$/)]]
        })
    });

    ngOnInit(): void {
        const param = this.route.snapshot.paramMap.get('id');
        if (!param) return;                       // create mode

        const id = Number(param);
        this.id.set(id);
        this.service.getById(id).subscribe({
            next: p => this.form.patchValue(p),
            error: () => this.loadError.set('Patient not found.')
        });
    }

    save(): void {
        this.formError.set(null);

        if (this.form.invalid) {
            this.form.markAllAsTouched();           // so validation messages appear immediately
            return;
        }

        this.saving.set(true);
        const data = this.form.getRawValue() as unknown as Patient;
        const id = this.id();

        const request = id === null
            ? this.service.create(data)
            : this.service.update(id, data);

        request.subscribe({
            next: () => this.router.navigate(['/patients']),
            error: (err: HttpErrorResponse) => {
                this.saving.set(false);
                this.showServerErrors(err);
            }
        });
    }

    /** Maps backend error messages onto the matching form controls. */
    private showServerErrors(err: HttpErrorResponse): void {
        if (err.status === 400 && err.error?.fieldErrors) {
            for (const [field, message] of Object.entries(err.error.fieldErrors)) {
                this.form.get(field)?.setErrors({ server: message });
            }
            this.formError.set('Some fields are not valid.');
            return;
        }
        if (err.status === 409) {
            this.form.get('pid')?.setErrors({ server: err.error?.message ?? 'PID already in use' });
            this.formError.set('PID is already used by another patient.');
            return;
        }
        this.formError.set('Failed to save. Make sure the backend is running.');
    }

    /** Used by the template to decide whether to show an error message. */
    hasError(path: string): boolean {
        const c = this.form.get(path);
        return !!c && c.invalid && (c.touched || c.dirty);
    }

    errorMessage(path: string): string {
        const c = this.form.get(path);
        if (!c?.errors) return '';
        const e = c.errors;
        if (e['server']) return e['server'];
        if (e['required']) return 'Required';
        if (e['maxlength']) return `Maximum ${e['maxlength'].requiredLength} characters`;
        if (e['pastDate']) return 'Date of birth must be in the past';
        if (e['pattern']) return path.endsWith('postcode') ? 'Postcode must be 4 digits'
            : 'Invalid format';
        return 'Invalid';
    }
}