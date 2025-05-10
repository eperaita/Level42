import SwiftUI
import shared

struct SelectedProjectView: View {
    let projectId: Int  // Solo recibimos el ID
    @Binding var navigationPath: NavigationPath
    
    // Buscamos el proyecto anidado en el perfil (igual que en Android)
    private var project: Project? {
        SessionManager.shared.selectedUserProfile?.projects.first { $0.project.id == projectId }
    }
    
    // Diseño idéntico a tu versión Android
    var body: some View {
        ZStack {
            // Fondo amarillo
            Color(red: 1.0, green: 0.988, blue: 0.0).ignoresSafeArea()
            
            if let project = project {
                VStack(spacing: 0) {
                    Spacer().frame(height: UIScreen.main.bounds.height * 0.2)
                    
                    // Circle con nombre (como en Android)
                    ZStack {
                        Circle()
                            .fill(Color.black)
                            .frame(width: 220, height: 220)
                        
                        Text(project.project.name)  // project.project.name por anidación
                            .font(.system(size: 28, weight: .bold))
                            .foregroundColor(.white)
                            .multilineTextAlignment(.center)
                            .padding(20)
                    }
                    
                    Spacer().frame(height: 20)
                    
                    // Info del proyecto
                    VStack(alignment: .center, spacing: 12) {
                        if let finalMark = project.finalMark {
                            Text("Final Mark: \(finalMark)")
                                .font(.system(size: 20))
                        }
                        
                        Text("Status: \(project.status)")
                            .font(.system(size: 20))
                        
                        Text("Updated At: \(formattedDate(from: project.updatedAt))")
                            .font(.system(size: 20))
                    }
                    .foregroundColor(.black)
                    .padding(.horizontal, 32)
                    
                    Spacer()
                }
            } else {
                // Vista de error (como en Android)
                VStack {
                    Text("Proyecto no encontrado")
                        .foregroundColor(.red)
                        .font(.system(size: 20))
                    
                    Button("Volver") {
                        navigationPath.removeLast()
                    }
                    .padding()
                    .background(Color.black)
                    .foregroundColor(Color(red: 1.0, green: 0.988, blue: 0.0))
                    .cornerRadius(8)
                }
                .onAppear {
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                        navigationPath.removeLast()  // Vuelve atrás automáticamente
                    }
                }
            }
            
            // Botón Atrás (idéntico a Android)
            Button(action: { navigationPath.removeLast() }) {
                Image(systemName: "chevron.left")
                    .font(.system(size: 24, weight: .bold))
                    .foregroundColor(Color(red: 1.0, green: 0.988, blue: 0.0))
                    .frame(width: 50, height: 50)
                    .background(Color.black)
                    .clipShape(Circle())
            }
            .padding(.leading, 16)
            .padding(.top, 16)
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
        }
        .navigationBarBackButtonHidden(true)
        .onAppear {
            print("✅ Proyecto encontrado: \(project != nil ? "Sí (\(project!.project.name))" : "No")")
        }
    }
    
    func formattedDate(from isoString: String) -> String {
        let isoFormatter = ISO8601DateFormatter()
        isoFormatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        
        let displayFormatter = DateFormatter()
        displayFormatter.dateFormat = "dd MMM yyyy, HH:mm"
        displayFormatter.locale = Locale(identifier: "en_US_POSIX")

        if let date = isoFormatter.date(from: isoString) {
            return displayFormatter.string(from: date)
        } else {
            return "Invalid date"
        }
    }
}
