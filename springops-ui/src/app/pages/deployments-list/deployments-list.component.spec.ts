import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeploymentsListComponent } from './deployments-list.component';

describe('DeploymentsListComponent', () => {
  let component: DeploymentsListComponent;
  let fixture: ComponentFixture<DeploymentsListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DeploymentsListComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DeploymentsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
