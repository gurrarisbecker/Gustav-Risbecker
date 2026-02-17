
package se.kth.view;

import java.util.Scanner;

import se.kth.controller.TeachingManager;

public class TeachingAllocationCLI {

    private final TeachingManager manager = new TeachingManager();
    private final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        new TeachingAllocationCLI().run();
    }

    public void run() {
        boolean running = true;
        System.out.println("--- Välkommen till Kursallokeringssystemet ---");
        
        while (running) {
            displayMenu();
            
            try {

                String input = scanner.nextLine();
                int choice = Integer.parseInt(input);
                
                switch (choice) {
                    case 1: computeCost(); break;
                    case 2: modifyStudents(); break;
                    case 3: allocateDeallocate(); break;
                    case 4: addNewActivity(); break;
                    case 5: running = false; break;
                    default: System.out.println("Ogiltigt val");
                }
            } catch (NumberFormatException e) {
                System.err.println("Fel, välj 1-5");
            } catch (Exception e) {
                System.err.println("Error:" + e.getMessage());
            }
        }
        scanner.close();
    }
    

    private void displayMenu() {
        System.out.println("1. Beräkna kostnad");
        System.out.println("2. lägg till studenter");
        System.out.println("3. Lägg till ta bort lärare");
        System.out.println("4. Lägg till excerise");
        System.out.println("5. Avsluta");
        System.out.print("Välj: ");
    }
    

private String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    
private void computeCost() throws Exception {
        String idStr = readString("vilken instance_id?");
        int instanceId = Integer.parseInt(idStr);

        String report = manager.computeTeachingCost(instanceId);
        
        System.out.println("\n" + report);
    }

private void modifyStudents() throws Exception {
        String idStr = readString("VIlken instance_id?");
        int instanceId = Integer.parseInt(idStr);
        
        String increaseStr = readString("Öka antal studenter med: ");
        int increase = Integer.parseInt(increaseStr);

        String result = manager.increaseStudentsAndShowCost(instanceId, increase);
        
        System.out.println("\n" + result);
    }
    
private void allocateDeallocate() throws Exception {

        String teacherId = readString("Ange Lärare (E001)");

        String idStr = readString("Vilken insatnce_id");
        int instanceId = 0;
        try {
            instanceId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            System.out.println("Fel isnatnce_id");
            return;
        }
        System.out.println("1. Lägg till");
        System.out.println("2. ta bort");
        String choice = readString("Välj");

        if (choice.equals("1")) {
            manager.allocateTeacher(teacherId, instanceId);
        } else if (choice.equals("2")) {
            manager.deallocateTeacher(teacherId, instanceId);
        } else {
            System.out.println("Ogiltigt val, avbryter.");
        }
    }
    
private void addNewActivity() throws Exception {

        String idStr = readString("Vilken instance_id?");
        int instanceId = 0;
        try {
            instanceId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            System.out.println("Fel: ID");
            return;
        }
        String teacherId = readString("Ange lärare (E001):");

        String report = manager.addNewActivityAndAllocate(instanceId, teacherId);
        
        System.out.println(report);
    }
}
