import { TestBed } from '@angular/core/testing';

import { ApplicationEnvService } from './application-env.service';

describe('ApplicationEnvService', () => {
  let service: ApplicationEnvService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ApplicationEnvService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
