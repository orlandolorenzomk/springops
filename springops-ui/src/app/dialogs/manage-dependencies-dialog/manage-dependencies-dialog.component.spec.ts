import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ManageDependenciesDialogComponent } from './manage-dependencies-dialog.component';

describe('ManageDependenciesDialogComponent', () => {
  let component: ManageDependenciesDialogComponent;
  let fixture: ComponentFixture<ManageDependenciesDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ManageDependenciesDialogComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ManageDependenciesDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
