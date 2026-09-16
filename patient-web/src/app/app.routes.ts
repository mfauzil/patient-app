import { Routes } from '@angular/router';
import { PatientForm } from './patients/patient-form/patient-form';
import { PatientList } from './patients/patient-list/patient-list';

export const routes: Routes = [
  { path: '', redirectTo: 'patients', pathMatch: 'full' },
  { path: 'patients', component: PatientList },
  { path: 'patients/new', component: PatientForm },
  { path: 'patients/:id/edit', component: PatientForm },
  { path: '**', redirectTo: 'patients' }
];
