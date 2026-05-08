import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

@Component({
  selector: 'app-add-trip',
  standalone: true,
  imports: [FormsModule],
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

  constructor(private http: HttpClient, private router: Router) {}

  get isValid(): boolean {
    return !!this.name && !!this.destination && !!this.startDate && !!this.endDate && this.endDate >= this.startDate;
  }

  submit(): void {
    if (!this.isValid) return;
    this.loading = true;
    this.errorMsg = '';

    this.http.post('http://localhost:8080/api/trips', {
      name: this.name,
      destination: this.destination,
      startDate: this.startDate,
      endDate: this.endDate
    }).subscribe({
      next: () => { this.router.navigate(['/calendar']); },
      error: () => { this.errorMsg = 'Fehler — ist das Backend gestartet?'; this.loading = false; }
    });
  }

  reset(): void {
    this.name = '';
    this.destination = '';
    this.startDate = '';
    this.endDate = '';
    this.success = false;
    this.errorMsg = '';
  }
}
