import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-add-trip',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './add-trip.html',
  styleUrl: './add-trip.css'
})
export class AddTripComponent {
  name = '';
  destination = '';
  startDate = '';
  endDate = '';

  success = false;
  errorMsg = '';
  loading = false;
  extracting = false;

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  get isValid(): boolean {
    return !!this.name &&
      !!this.destination &&
      !!this.startDate &&
      !!this.endDate &&
      this.endDate >= this.startDate;
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];

    if (!file) {
      return;
    }

    this.errorMsg = '';
    this.extracting = true;

    const formData = new FormData();
    formData.append('file', file);

    this.http.post<any>('http://localhost:8080/api/extract', formData).subscribe({
      next: (data) => {
        if (data?.name) {
          this.name = data.name;
        }
        if (data?.destination) {
          this.destination = data.destination;
        }
        if (data?.startDate) {
          this.startDate = data.startDate;
        }
        if (data?.endDate) {
          this.endDate = data.endDate;
        }

        this.extracting = false;
      },
      error: () => {
        this.errorMsg = 'Extraktion fehlgeschlagen. Bitte Daten manuell eingeben.';
        this.extracting = false;
      }
    });
  }

  submit(): void {
    if (!this.isValid) {
      return;
    }

    this.loading = true;
    this.errorMsg = '';

    this.http.post('http://localhost:8080/api/trips', {
      name: this.name,
      destination: this.destination,
      startDate: this.startDate,
      endDate: this.endDate
    }).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/calendar']);
      },
      error: () => {
        this.errorMsg = 'Fehler - ist das Backend gestartet?';
        this.loading = false;
      }
    });
  }

  reset(): void {
    this.name = '';
    this.destination = '';
    this.startDate = '';
    this.endDate = '';
    this.success = false;
    this.errorMsg = '';
    this.loading = false;
    this.extracting = false;
  }
}
