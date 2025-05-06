import SwiftUI
import shared

struct SkillsView: View {
    @EnvironmentObject private var viewModel: ProfileViewModel
    @Binding var navigationPath: NavigationPath // Recibe el path

    private let customYellow = Color(red: 1.0, green: 0.988, blue: 0.0)

    var body: some View {
        ZStack {
            customYellow.edgesIgnoringSafeArea(.all)
            
            // Contenido principal
            if let profile = SessionManager.shared.userProfile {
                if let skills = profile.cursus_users.first(where: { $0.cursus.id == 21 })?.skills,
                   !skills.isEmpty {
                    VStack {
                        Text("SKILLS")
                            .font(.system(size: 34, weight: .bold))
                            .foregroundColor(.black)
                            .padding(.bottom, 24)

                        Spacer()

                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 24) {
                                ForEach(skills.compactMap { $0 }.sorted { $0.level > $1.level }, id: \.name) { skill in
                                    VerticalSkillBar(
                                        name: skill.name,
                                        percentage: Int((skill.level / 10.0) * 100)
                                    )
                                }
                            }
                            .padding(.horizontal, 16)
                        }
                        
                        Spacer()
                        
                        Button("BACK") {
                            navigationPath.removeLast()
                        }
                        .buttonStyle(BlackButtonStyle())
                        .padding(.bottom, 24)
                        .padding(.horizontal, 20)
                    }
                    .padding(.top, 16)
                } else {
                    Text("No skills found")
                        .foregroundColor(.black)
                        .font(.system(size: 18))
                }
            } else {
                Text("Perfil no disponible")
                    .foregroundColor(.black)
                    .font(.system(size: 20, weight: .bold))
            }
        }
        .navigationBarBackButtonHidden(true) // Oculta el botón Back
        .navigationBarHidden(true) // Oculta toda la barra de navegación
    }
}

struct VerticalSkillBar: View {
    let name: String
    let percentage: Int
    @State private var progress: CGFloat = 0
    
    var body: some View {
        VStack {
            ZStack(alignment: .bottom) {
                // Barra de fondo
                RoundedRectangle(cornerRadius: 8)
                    .fill(Color.gray)
                    .frame(width: 70, height: 300)
                
                // Barra de progreso
                RoundedRectangle(cornerRadius: 8)
                    .fill(Color.black)
                    .frame(width: 70, height: 300 * progress)
                    .animation(.easeInOut(duration: 1.5), value: progress)
            }
            
            Text(name)
                .font(.system(size: 14, weight: .bold))
                .foregroundColor(.black)
                .frame(width: 100)
                .multilineTextAlignment(.center)
                .lineLimit(2)
                .padding(.top, 8)
        }
        .onAppear {
            progress = CGFloat(percentage) / 100.0
        }
    }
}

struct BlackButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.system(size: 18, weight: .bold))
            .foregroundColor(.yellow)
            .frame(maxWidth: .infinity)
            .padding()
            .background(Color.black)
            .cornerRadius(8)
    }
}
