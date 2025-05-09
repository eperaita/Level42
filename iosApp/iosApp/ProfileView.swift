import SwiftUI
import shared

struct ProfileView: View {
    @EnvironmentObject private var viewModel: ProfileViewModel
    @Binding var navigationPath: NavigationPath
    
    var body: some View {
        // Protección contra perfil nulo
        if SessionManager.shared.selectedUserProfile == nil {
            EmptyView()
                .onAppear {
                    navigationPath.removeLast(navigationPath.count)
                    navigationPath.append("login")
                }
        } else {
            ZStack {
                Color.black.ignoresSafeArea()
                
                UserProfileContent(
                    user: SessionManager.shared.selectedUserProfile!,
                    navigationPath: $navigationPath,
                    viewModel: viewModel
                )
                
                ButtonBack(action: {
                    viewModel.clearSearch()
                    navigationPath.append("welcome")
                })
                .position(x: 30, y: 50)
            }
            .edgesIgnoringSafeArea(.all)
            .navigationBarBackButtonHidden(true)
            .navigationBarHidden(true)
        }
    }
}

// Componente reutilizable
private struct UserProfileContent: View {
    let user: SelectedUserProfile
    @Binding var navigationPath: NavigationPath
    @ObservedObject var viewModel: ProfileViewModel
    
    // Styles
    private let customYellow = Color(red: 1.0, green: 0.988, blue: 0.0)
    private let avatarSize: CGFloat = 220
    private let buttonHeight: CGFloat = 50
    private let horizontalPadding: CGFloat = 40
    
    var body: some View {
        GeometryReader { geometry in
            VStack(spacing: 0) {
                // AVATAR
                VStack {
                    ZStack {
                        Circle()
                            .fill(customYellow)
                            .frame(width: avatarSize, height: avatarSize)
                        
                        if let imageUrl = user.image?.link, let url = URL(string: imageUrl) {
                            AsyncImage(url: url) { phase in
                                if case .success(let image) = phase {
                                    image
                                        .resizable()
                                        .scaledToFill()
                                        .frame(width: avatarSize, height: avatarSize)
                                        .clipShape(Circle())
                                } else {
                                    Circle().fill(Color.gray)
                                }
                            }
                        } else {
                            Circle().fill(Color.gray)
                        }
                    }
                    .padding(.top, 90)
                    .padding(.bottom, 30)
                }
                .padding(.top, UIApplication.shared.windows.first?.safeAreaInsets.top ?? 0)
                .padding(.bottom, 30)
                
                // INFO
                VStack(spacing: 12) {
                    Text(user.login)
                        .font(.system(size: 22, weight: .bold))
                        .padding(.bottom, 4)
                    
                    Text("\(user.first_name ?? "") \(user.last_name ?? "")")
                        .font(.system(size: 16))
                    
                    Text("email: \(user.email)")
                        .font(.system(size: 16))
                    
                    Text("Level: \(user.level ?? 0)")
                        .font(.system(size: 16))
                    
                    Text("Wallet: \(user.wallet)")
                        .font(.system(size: 16))
                }
                .foregroundColor(.white)
                .multilineTextAlignment(.center)
                .padding(.horizontal, horizontalPadding)
                .padding(.bottom, 50)
                
                // BOTONES
                VStack(spacing: 30) {
                    customButton(
                        title: "PROJECTS",
                        action: {
                            viewModel.loadProjectsForUser(login: user.login)
                            navigationPath.append("projects")
                        }
                    )
                    
                    customButton(
                        title: "SKILLS",
                        action: {
                            navigationPath.append("skills")
                        }
                    )
                }
                .padding(.horizontal, horizontalPadding)
                .padding(.bottom, 50)
            }
            .frame(width: geometry.size.width, height: geometry.size.height)
        }
    }
    
    private func customButton(title: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .font(.system(size: 18, weight: .bold))
                .padding(.horizontal, 20)
                .padding(.vertical, 10)
                .frame(maxWidth: .infinity)
                .frame(height: buttonHeight)
                .foregroundColor(customYellow)
                .overlay(
                    RoundedRectangle(cornerRadius: 8)
                        .stroke(customYellow, lineWidth: 2)
                )
        }
    }
}

// Componente ButtonBack (debe estar en otro archivo para reutilizar)
struct ButtonBack: View {
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            Image(systemName: "chevron.left")
                .foregroundColor(Color(red: 1.0, green: 0.988, blue: 0.0))
                .padding()
                .background(Color.black)
                .clipShape(Circle())
        }
    }
}
