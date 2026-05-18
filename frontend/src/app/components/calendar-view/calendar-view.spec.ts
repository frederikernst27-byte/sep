import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';

import { CalendarView } from './calendar-view';

describe('CalendarView', () => {
  let component: CalendarView;
  let fixture: ComponentFixture<CalendarView>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CalendarView],
      providers: [provideHttpClient(), provideHttpClientTesting()]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CalendarView);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
    httpMock.expectOne('http://localhost:8080/api/trips').flush([]);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should switch between month, week, day and list without changing trips', () => {
    const trips = [
      {
        id: 1,
        name: 'Sommerurlaub',
        destination: 'Barcelona',
        startDate: '2026-07-10',
        endDate: '2026-07-14'
      }
    ];
    component.trips = trips;

    component.setView('week');
    expect(component.activeView).toBe('week');
    expect(component.trips).toEqual(trips);

    component.setView('day');
    expect(component.activeView).toBe('day');
    expect(component.trips).toEqual(trips);

    component.setView('list');
    expect(component.activeView).toBe('list');
    expect(component.sortedTrips[0].trip).toEqual(trips[0]);

    component.setView('month');
    expect(component.activeView).toBe('month');
    expect(component.trips).toEqual(trips);
  });
});
