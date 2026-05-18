import { CalendarEntry } from './calendar-entry.model';

describe('CalendarEntry', () => {
  it('should create an instance', () => {
    const entry: CalendarEntry = {
      id: 1,
      name: 'Testeintrag',
      description: 'Beschreibung'
    };

    expect(entry).toBeTruthy();
  });
});
